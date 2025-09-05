package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.*
import org.sdpi.asciidoc.factories.ContentModuleTableBuilder
import org.sdpi.asciidoc.factories.OidTableBuilder
import org.sdpi.asciidoc.factories.TransactionTableBuilder
import org.sdpi.asciidoc.model.*

/**
 * Tree processor to populate requirement table placeholders, which are inserted
 * by Add*Placeholder block macros.
 */
class PopulateTables(private val docInfo: SdpiInformationCollector) : Treeprocessor() {
    private companion object : Logging;

    override fun process(document: Document): Document {
        processBlock(document as StructuralNode)
        return document
    }

    private fun processBlock(block: StructuralNode) {
        if (block.hasRole(Roles.QueryTable.REQUIREMENT.key)) {
            populateRequirementTable(block as Table, getSelectedRequirements(block))
        } else if (block.hasRole(ICS_TABLE_ROLE)) {
            populateICSTable(block as Table, getSelectedRequirements(block))
        } else if (block.hasRole(Roles.QueryTable.TRANSACTIONS.key)) {
            populateTransactionTable(block as Table)
        } else if (block.hasRole(Roles.QueryTable.CONTENT_MODULE.key)) {
            populateContentModuleTable(block as Table)
        } else if (block.hasRole(Roles.QueryTable.OID.key)) {
            populateOidTable(block as Table)
        } else {
            for (child in block.blocks) {
                processBlock(child)
            }
        }
    }

    // region requirement table
    /**
     * Determine which requirements should be included in the table.
     */
    private fun getSelectedRequirements(block: Table): Collection<SdpiRequirement2> {
        val requirementsInDocument = docInfo.requirements()

        val aGroups = getRequirementGroups(block.attributes[RequirementAttributes.Common.GROUPS.key])
        val aActors = getRequirementActors(block.attributes[RequirementAttributes.Common.ACTOR.key])
        if (aGroups.isEmpty() && aActors.isEmpty()) {
            // unfiltered
            return requirementsInDocument.values.sortedBy { it.requirementNumber }
        }

        val bAllGroups = aGroups.isEmpty()
        val bAllActors = aActors.isEmpty()

        val selectedRequirements = requirementsInDocument
            .values
            .filter { it ->
                (bAllGroups || it.groups.any { it in aGroups })
                        && (bAllActors || it.actors().any { it in aActors })
            }
            .sortedBy { it.requirementNumber }
        return selectedRequirements
    }

    /**
     * Populates the table with the supplied requirements
     */
    private fun populateRequirementTable(table: Table, requirements: Collection<SdpiRequirement2>) {
        val colId = createTableColumn(table, 0)
        val colLocalId = createTableColumn(table, 1)
        val colLevel = createTableColumn(table, 2)
        val colType = createTableColumn(table, 3)

        val header = createTableRow(table)
        table.header.add(header)

        header.cells.add(createTableCell(colId, "Id"))
        header.cells.add(createTableCell(colLocalId, "Local id"))
        header.cells.add(createTableCell(colLevel, "Level"))
        header.cells.add(createTableCell(colType, "Type"))

        for (req in requirements) {
            val strGlobalId = req.globalId
            val level = req.level
            val strType = req.getTypeDescription()

            val strIdLink = req.makeLinkGlobal()

            val cellGlobalId = createDocument(table.document)
            cellGlobalId.blocks.add(
                createBlock(
                    cellGlobalId,
                    plainContext(Contexts.PARAGRAPH),
                    strGlobalId,
                    mapOf("role" to "global-id")
                )
            )

            val row = createTableRow(table)
            row.cells.add(createTableCell(colId, cellGlobalId))
            row.cells.add(createTableCell(colLocalId, strIdLink))
            row.cells.add(createTableCell(colLevel, level.keyword))
            row.cells.add(createTableCell(colType, strType))

            table.body.add(row)
        }
    }
    // endregion

    // region ICS table
    private fun populateICSTable(table: Table, requirements: Collection<SdpiRequirement2>) {
        val colId = createTableColumn(table, 0)
        val colReference = createTableColumn(table, 1)
        val colStatus = createTableColumn(table, 2)
        val colSupport = createTableColumn(table, 3)
        val colComment = createTableColumn(table, 4)

        val header = createTableRow(table)
        table.header.add(header)

        header.cells.add(createTableCell(colId, "Index"))
        header.cells.add(createTableCell(colReference, "Reference"))
        header.cells.add(createTableCell(colStatus, "Status"))
        header.cells.add(createTableCell(colSupport, "Support"))
        header.cells.add(createTableCell(colComment, "Comment"))

        for (req in requirements) {
            val strIcsId = String.format("ICS-%04d", req.requirementNumber)
            val strReference = req.makeLinkGlobal()
            val level = req.level

            val row = createTableRow(table)
            row.cells.add(createTableCell(colId, strIcsId))
            row.cells.add(createTableCell(colReference, strReference))
            row.cells.add(createTableCell(colStatus, level.icsStatus))
            row.cells.add(createTableCell(colSupport, ""))
            row.cells.add(createTableCell(colComment, ""))

            table.body.add(row)
        }
    }
    // endregion

    // region transaction table
    private fun populateTransactionTable(table: Table) {

        val strProfile = table.attributes[Roles.Profile.ID.key]?.toString()
        checkNotNull(strProfile) {
            logger.error("Table missing required attribute '${Roles.Profile.ID.key}'")
        }

        val strProfileOption = table.attributes[Roles.Profile.ID_PROFILE_OPTION.key]?.toString()
        logger.info("Table profile option = $strProfileOption")
        val profileFilter: Pair<OptionType, String>? = if (strProfileOption == null) {
            null
        } else Pair(OptionType.PROFILE, strProfileOption)

        val strActorId = table.attributes[Roles.Transaction.ACTOR_ID.key]?.toString()

        val profile: SdpiProfile? = docInfo.getProfile(strProfile)
        checkNotNull(profile) {
            logger.error("Unknown profile $strProfile")
        }

        val tableBuilder = TransactionTableBuilder(this, table, strActorId == null)
        tableBuilder.setupHeadings()

        if (strActorId != null) {
            val actor = profile.getActor(strActorId)
            checkNotNull(actor) {
                logger.error("Actor $strActorId is not defined in profile $strProfile")
            }
            addActorTransactions(tableBuilder, profile, actor, profileFilter)
        } else {
            for (actor in profile.actorReferences()) {
                addActorTransactions(tableBuilder, profile, actor, profileFilter)
            }
        }
    }

    private fun addActorTransactions(
        tableBuilder: TransactionTableBuilder,
        profile: SdpiProfile,
        actor: SdpiActor,
        optionFilter: Pair<OptionType, String>?,
    ) {
        var bFirstActor = true
        val transactionReferences = profile.transactionReferences
        if (transactionReferences != null) {
            for (transactionReference: SdpiTransactionReference in transactionReferences.sortedBy { it.transactionId }) {
                val strTransactionId = transactionReference.transactionId
                val transaction: SdpiTransaction? = getTransaction(transactionReference)
                checkNotNull(transaction) {
                    logger.error("Unknown transaction id $strTransactionId")
                }

                for (obl in transactionReference.obligations) {
                    if (obl.actorId == actor.id) {
                        var bFirstTransaction = true

                        val obligationsForTransaction =
                            profile.getTransactionObligations(strTransactionId, actor.id, optionFilter)
                        for (ref in obligationsForTransaction) {
                            tableBuilder.addRow(
                                if (bFirstActor) actor else null,
                                if (bFirstTransaction) transaction else null,
                                ref.contribution,
                                ref.obligation,
                                ref.optionId
                            )
                            bFirstTransaction = false
                            bFirstActor = false
                        }

                    }
                }
            }
        }

        // If no transactions were added, add an empty row.
        if (bFirstActor) {
            tableBuilder.addActorOnlyRow(actor)
        }
    }

    private fun getTransaction(transactionReference: SdpiTransactionReference): SdpiTransaction? {
        val strTransactionId = transactionReference.transactionId
        val knownTransaction: SdpiTransaction? = docInfo.transactions()[strTransactionId]
        if (null != knownTransaction) {
            return knownTransaction
        }

        if (transactionReference.placeholderName != null) {
            val placeholderTransaction = SdpiTransaction(
                transactionReference.transactionId, emptyList<String>(),
                transactionReference.placeholderName, "",null
            )
            return placeholderTransaction
        }

        return null
    }
    // endregion

    // region content module table
    private fun populateContentModuleTable(table: Table) {

        val strProfile = table.attributes[Roles.Profile.ID.key]?.toString()
        checkNotNull(strProfile) {
            logger.error("Table missing required attribute '${Roles.Profile.ID.key}'")
        }
        val strProfileOption = table.attributes[Roles.Profile.ID_PROFILE_OPTION.key]?.toString()

        val profile: SdpiProfile? = docInfo.getProfile(strProfile)
        checkNotNull(profile) {
            logger.error("Unknown profile $strProfile")
        }

        val tableBuilder = ContentModuleTableBuilder(this, table)
        tableBuilder.setupHeadings()

        for (actorRef in profile.actorReferences()) {
            addActorContentModule(tableBuilder, profile, actorRef, strProfileOption)
        }
    }

    private fun addActorContentModule(
        tableBuilder: ContentModuleTableBuilder,
        profile: SdpiProfile,
        actor: SdpiActor,
        strProfileOptionFilter: String?,
    ) {
        var bFirstActor = true
        val references = profile.contentModuleReferences
        if (references != null) {
            for (ref: SdpiContentModuleRef in references.filter { it.actorId == actor.id }) {
                val strRefId = ref.contentModuleId
                val module: SdpiContentModule? = getContentModule(ref)
                checkNotNull(module) {
                    logger.error("Unknown content-module id $strRefId")
                }

                tableBuilder.addRow(
                    if (bFirstActor) actor else null,
                    module,
                    ref.obligation,
                    null
                )
                bFirstActor = false
            }
        }

        for (option in profile.options) {
            for (ref: SdpiContentModuleRef in option.contentModuleReferences.filter { it.actorId == actor.id }) {
                val strRefId = ref.contentModuleId
                val module: SdpiContentModule? = getContentModule(ref)
                checkNotNull(module) {
                    logger.error("Unknown content-module id $strRefId")
                }

                tableBuilder.addRow(
                    if (bFirstActor) actor else null,
                    module,
                    ref.obligation,
                    option.label
                )
            }
        }
    }

    private fun getContentModule(reference: SdpiContentModuleRef): SdpiContentModule? {
        val strId = reference.contentModuleId
        val knownContentModule: SdpiContentModule? = docInfo.contentModules()[strId]
        if (null != knownContentModule) {
            return knownContentModule
        }

        if (reference.placeholderName != null) {
            val placeholder = SdpiContentModule(
                reference.contentModuleId,
                reference.placeholderName, ""
            )
            return placeholder
        }

        return null
    }
    // endregion

    // region oid table
    private fun populateOidTable(table: Table) {

        val strRootArcs = table.attributes[TableAttributes.OidTable.ROOT_ARC.key]?.toString()
        checkNotNull(strRootArcs) {
            logger.error("$BLOCK_MACRO_NAME_OID_TABLE missing required attribute '${TableAttributes.OidTable.ROOT_ARC.key}'")
        }

        val oidsToTable = mutableListOf<SdpiOidReference>()
        for (strArc in strRootArcs.split(' ')) {
            val arcOid = parseOidId(strArc)
            checkNotNull(arcOid) {
                logger.error("Arc $strArc is not known")
            }

            if (arcOid == WellKnownOid.DEV_ACTOR) {
                gatherActorOids(oidsToTable)
            } else if (arcOid == WellKnownOid.DEV_TRANSACTION) {
                gatherTransactionOids(oidsToTable)
            } else if (arcOid == WellKnownOid.DEV_PROFILE) {
                gatherProfileOids(oidsToTable)
            } else {
                logger.error("Oid tables don't support $strArc (yet?)")
            }
        }

        val tableBuilder = OidTableBuilder(this, table)
        tableBuilder.setupHeadings()

        for (oid in oidsToTable.sortedBy{it}) {
            tableBuilder.addRow(oid)
        }
    }

    private fun gatherActorOids(oidsToTable: MutableList<SdpiOidReference>) {
        for(profile in docInfo.profiles()) {
            for(actor in profile.actorReferences()) {
                for(strOid in actor.oids) {
                    val oid = SdpiOidReference(WellKnownOid.DEV_ACTOR, strOid, actor.label, actor.anchor)
                    oidsToTable.add(oid)
                }
            }
        }
    }

    private fun gatherTransactionOids(oidsToTable: MutableList<SdpiOidReference>) {
        for(transaction in docInfo.transactions().values) {
            for(strOid in transaction.oids) {
                val oid = SdpiOidReference(
                    WellKnownOid.DEV_TRANSACTION,
                    strOid,
                    transaction.label,
                    transaction.anchor
                )
                oidsToTable.add(oid)
            }
        }
    }

    private fun gatherProfileOids(oidsToTable: MutableList<SdpiOidReference>) {
        for(profile in docInfo.profiles()) {
            for(strOid in profile.oids) {
                val oid = SdpiOidReference(
                    WellKnownOid.DEV_PROFILE,
                    strOid,
                    profile.label,
                    profile.anchor
                )
                oidsToTable.add(oid)
            }
        }
    }


    // endregion
}

