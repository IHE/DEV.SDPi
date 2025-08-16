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
        var contribution: Contribution? = null
        val description = mutableListOf<String>()

        for (strLine in blockLines) {
            if (strLine.startsWith('[')) {
                if (strActor != null && contribution != null && description.isNotEmpty()) {
                    roles.add(SdpiActorRole(strActor, contribution, description.toList()))
                }
                description.clear()

                val result = parseContributorAttributes(strLine)
                strActor = result.first
                contribution = result.second
            } else {
                description.add(strLine)
            }
        }

        if (strActor != null && contribution != null && description.isNotEmpty()) {
            roles.add(SdpiActorRole(strActor, contribution, description.toList()))
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
        header.cells.add(createTableCell(colDescription, "Description"))


        for (actorRole in roles) {
            val strActorId = "RefActor:${actorRole.actorId}[]"
            val contribution = actorRole.contribution
            val strDescription = actorRole.description.joinToString("\r\n")

            val row = createTableRow(roleTable)
            row.cells.add(createTableCell(colActor, strActorId))
            row.cells.add(createTableCell(colContribution, contribution.keyword))

            row.cells.add(createTableCell(colDescription, strDescription))

            roleTable.body.add(row)
        }

        return roleTable
    }

    private fun parseContributorAttributes(strLine: String): Pair<String, Contribution> {
        val attributes = parseAttributes(strLine)

        val strActorId = attributes[Roles.Transaction.ACTOR_ID.key]
        checkNotNull(strActorId) {
            logger.error("Actor in transaction is missing an actor id")
        }
        val strContribution = attributes[Roles.Transaction.CONTRIBUTION.key]
        checkNotNull(strContribution) {
            logger.error("Actor $strActorId in transaction is missing a contribution")
        }
        val contribution = parseContribution(strContribution)
        checkNotNull(contribution) {
            logger.error("Actor $strActorId's contribution '$strContribution' is not recognized")
        }

        return Pair(strActorId, contribution)
    }

    private fun parseAttributes(strValue: String): Map<String, String> {
        val attributes = mutableMapOf<String, String>()

        for (match in RE_ATTRIBUTES.findAll(strValue)) {
            val (key, value) = match.destructured
            attributes[key] = value
        }

        return attributes
    }
}