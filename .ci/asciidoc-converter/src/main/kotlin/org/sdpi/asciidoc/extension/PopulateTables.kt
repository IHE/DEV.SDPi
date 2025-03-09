package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.BlockAttribute
import org.sdpi.asciidoc.RequirementAttributes
import org.sdpi.asciidoc.getRequirementGroups
import org.sdpi.asciidoc.model.SdpiRequirement2
import org.sdpi.asciidoc.plainContext

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
        if (block.hasRole(REQUIREMENT_TABLE_ROLE)) {
            populateQueryTable(block as Table, getSelectedRequirements(block))
        }
        if (block.hasRole(ICS_TABLE_ROLE)) {
            populateICSTable(block as Table, getSelectedRequirements(block))
        } else {
            for (child in block.blocks) {
                processBlock(child)
            }
        }
    }

    /**
     * Determine which requirements should be included in the table.
     */
    private fun getSelectedRequirements(block: Table): Collection<SdpiRequirement2> {
        val requirementsInDocument = docInfo.requirements()

        val aGroups = getRequirementGroups(block.attributes[RequirementAttributes.Common.GROUPS.key])
        if (aGroups.isEmpty()) {
            // unfiltered
            return requirementsInDocument.values
        }

        val selectedRequirements = requirementsInDocument.values.filter { it -> it.groups.any { it in aGroups } }
        return selectedRequirements
    }

    /**
     * Populates the table with the supplied requirements
     */
    private fun populateQueryTable(table: Table, requirements: Collection<SdpiRequirement2>) {
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

            val strIdLink = req.makeLink()

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

}