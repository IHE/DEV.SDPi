package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Table
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import org.sdpi.asciidoc.findIdFromParent
import org.sdpi.asciidoc.model.Contribution
import org.sdpi.asciidoc.model.SdpiActorRole
import org.sdpi.asciidoc.model.parseContribution

const val BLOCK_NAME_TRANSACTION_ACTORS = "sdpi_transaction_actors"

/**
 * Collects actor roles for transaction.
 * Each transaction in the document should contain a transaction actors block
 * which lists each of the actors and their roles in that transaction.
 */
@Name(BLOCK_NAME_TRANSACTION_ACTORS)
@Contexts(Contexts.OPEN)
@ContentModel(ContentModel.COMPOUND)
class TransactionActorsProcessor : BlockProcessor(BLOCK_NAME_TRANSACTION_ACTORS) {
    private companion object : Logging {
        val RE_ATTRIBUTES = """([a-zA-Z_-]+)\s*=\s*"?([^"\],]*)"?""".toRegex()
    }

    // Indexed by transaction id (e.g., DEV-23), this maps contains the roles
    // actors play in transactions.
    private val transactionActors = mutableMapOf<String, List<SdpiActorRole>>()

    fun transactionActors() = transactionActors

    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val strTransactionId = findIdFromParent(parent, Roles.Transaction.TRANSACTION.key, Roles.Transaction.TRANSACTION_ID.key)
        checkNotNull(strTransactionId) {
            logger.error("Missing ${Roles.Transaction.TRANSACTION_ID.key} on section with role of ${Roles.Transaction.TRANSACTION.key}")
        }
        if (transactionActors.containsKey(strTransactionId)) {
            logger.error("Actors for transaction $strTransactionId have already been defined.")
        }

        logger.info("Processing actors for transaction $strTransactionId. ")
        val roles = parseActorRoles(reader)
        transactionActors[strTransactionId] = roles
        return createActorRolesTable(parent, roles)
    }

    private fun parseActorRoles(reader: Reader): MutableList<SdpiActorRole> {
        val blockLines = reader.readLines()

        val roles = mutableListOf<SdpiActorRole>()

        var strActor: String? = null
        var contributions = mutableListOf<Contribution>()
        val description = mutableListOf<String>()

        for (strLine in blockLines) {
            if (strLine.startsWith('[')) {

                // remove trailing blank lines.
                while(description.isNotEmpty() && description.last().isEmpty()) {
                    description.removeLast()
                }

                if (strActor != null && contributions.isNotEmpty() && description.isNotEmpty()) {
                    roles.add(SdpiActorRole(strActor, contributions, description.toList()))
                }
                description.clear()

                val result = parseContributorAttributes(strLine)
                strActor = result.first
                contributions = result.second.toMutableList()
            } else {
                description.add(strLine)
            }
        }

        if (strActor != null && contributions.isNotEmpty() && description.isNotEmpty()) {
            roles.add(SdpiActorRole(strActor, contributions, description.toList()))
        }

        return roles
    }

    private fun TransactionActorsProcessor.createActorRolesTable(
        parent: StructuralNode,
        roles: MutableList<SdpiActorRole>
    ): Table? {
        val roleTable = createTable(parent)
        roleTable.attributes["role"] = Roles.Transaction.ACTOR_ROLE_TABLE.key
        roleTable.title = "Actor Roles [{var_transaction_id}]"

        val nDescriptionColumnIndex = 2

        val colActor = createTableColumn(roleTable, 0)
        val colContribution = createTableColumn(roleTable, 1)
        val colDescription = createTableColumn(roleTable, nDescriptionColumnIndex)

        val header = createTableRow(roleTable)
        roleTable.header.add(header)

        header.cells.add(createTableCell(colActor, "Actor"))
        header.cells.add(createTableCell(colContribution, "Contribution"))
        header.cells.add(createTableCell(colDescription, "Role"))


        for (actorRole in roles) {
            val strActorId = "RefActor:${actorRole.actorId}[]"
            val contributions = actorRole.contributions
            val strDescription = actorRole.description.joinToString("\r\n")

            val row = createTableRow(roleTable)
            row.cells.add(createTableCell(colActor, strActorId))
            row.cells.add(createTableCell(colContribution, contributions.joinToString { it.keyword }))

            row.cells.add(createTableCell(colDescription, strDescription))

            roleTable.body.add(row)
        }

        return roleTable
    }

    private fun parseContributorAttributes(strLine: String): Pair<String, List<Contribution>> {
        val attributes = parseAttributes(strLine)

        val actorId = attributes.firstOrNull{ it.first == Roles.Transaction.ACTOR_ID.key}
        checkNotNull(actorId) {
            logger.error("Actor in transaction is missing an actor id")
        }
        val strActorId = actorId.second
        val contributions = mutableListOf<Contribution>()
        for(contribution in attributes.filter{it.first == Roles.Transaction.CONTRIBUTION.key}) {
            val strContribution = contribution.second
            val cont = parseContribution(strContribution)
            checkNotNull(cont) {
                logger.error("Actor $strActorId's contribution '$strContribution' is not recognized")
            }
            contributions.add(cont)
        }

        check(contributions.isNotEmpty()) {
            logger.error("Actor $strActorId in transaction is missing a contribution")
        }

        return Pair(strActorId, contributions)
    }

    private fun parseAttributes(strValue: String): List<Pair<String, String>> {
        val attributes = mutableListOf<Pair<String, String>>()

        for (match in RE_ATTRIBUTES.findAll(strValue)) {
            val (key, value) = match.destructured
            attributes.add(Pair<String,String>(key, value))
        }

        return attributes
    }
}