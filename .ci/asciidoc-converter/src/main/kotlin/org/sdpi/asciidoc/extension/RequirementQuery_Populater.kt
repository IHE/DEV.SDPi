package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.BlockAttribute
import org.sdpi.asciidoc.getRequirementGroups
import org.sdpi.asciidoc.model.SdpiRequirement
import org.sdpi.asciidoc.model.SdpiRequirement2
import org.sdpi.asciidoc.model.StructuralNodeWrapper
import org.sdpi.asciidoc.model.toSealed
import org.sdpi.asciidoc.plainContext

/**
 * Tree processor to populate requirement table placeholders, which are inserted
 * by the RequirementTableMacroProcessor.
 */
class RequirementQuery_Populater(private val docInfo : SdpiInformationCollector): Treeprocessor()
{
    private companion object : Logging {
    }

    override fun process(document: Document): Document
    {
        processBlock(document as StructuralNode)
        return document
    }

    private fun processBlock(block: StructuralNode)
    {
        if (block.hasRole(REQUIREMENT_TABLE_ROLE))
        {
            populateTable(block as Table)
        }
        else
        {
            for(child in block.blocks)
            {
                processBlock(child)
            }
        }
    }

    /**
     * Processes a requirements list placeholder table by determining
     * which requirements should be included in the table and adding them.
     */
    private fun populateTable(block : Table)
    {
        logger.info {"Processing requirements list '${block.context}', attributes=${block.attributes}, role=${block.roles.size}"}

        val requirementsInDocument = docInfo.requirements()

        val aGroups = getRequirementGroups(block.attributes[BlockAttribute.REQUIREMENT_GROUPS.key])
        val selectedRequirements = requirementsInDocument.values.filter { it -> it.groups.any{ it in aGroups} }
        populateTable(block, selectedRequirements)
    }

    /**
     * Populates the table with the supplied requirements
     */
    private fun populateTable(table : Table, requirements : Collection<SdpiRequirement2>)
    {
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

        for (req in requirements)
        {
            val strGlobalId = req.globalId
            val level = req.level
            val strType = req.getTypeDescription()

            val strIdLink = req.makeLink()

            val cellGlobalId = createDocument(table.document)
            cellGlobalId.blocks.add(createBlock(cellGlobalId, plainContext(Contexts.PARAGRAPH), strGlobalId, mapOf("role" to "global-id")))

            val row = createTableRow(table)
            row.cells.add(createTableCell(colId, cellGlobalId))
            row.cells.add(createTableCell(colLocalId, strIdLink))
            row.cells.add(createTableCell(colLevel, level.keyword))
            row.cells.add(createTableCell(colType, strType))

            table.body.add(row)
        }
    }
}