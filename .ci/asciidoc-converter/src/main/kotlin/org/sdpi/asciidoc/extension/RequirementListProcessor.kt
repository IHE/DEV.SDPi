package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Treeprocessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.BlockAttribute
import org.sdpi.asciidoc.getRequirementGroups
import org.sdpi.asciidoc.model.SdpiRequirement
import org.sdpi.asciidoc.model.StructuralNodeWrapper
import org.sdpi.asciidoc.model.toSealed
import org.sdpi.asciidoc.plainContext

/**
 * Tree processor to populate requirement tables.
 * The requirement list macro processor inserts placeholder tables that we populate
 */
class RequirementListProcessor(private val requirements : RequirementsBlockProcessor): Treeprocessor()
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
        block.toSealed().let { node ->
            when (node) {
                is StructuralNodeWrapper.SdpiRequirementList ->
                {
                    processRequirementList(node.wrapped)
                }
                else ->
                {
                   // logger.debug { "Ignore block of type '${block.context}', attributes=${block.attributes}, role=${block.roles.size}" }
                }
            }
        }

        block.blocks.forEach { processBlock(it) }
    }

    /**
     * Processes a requirements list placeholder table by determining
     * which requirements should be included in the table and adding them.
     */
    private fun processRequirementList(block : Table)
    {
        logger.info {"Processing requirements list '${block.context}', attributes=${block.attributes}, role=${block.roles.size}"}

        val requirementsInDocument = requirements.detectedRequirements()

        val aGroups = getRequirementGroups(block.attributes[BlockAttribute.REQUIREMENT_GROUPS.key])
        val selectedRequirements = requirementsInDocument.values.filter { it.groups.any{ it in aGroups} }
        populateTable(block, selectedRequirements)
    }

    /**
     * Populates the table with the supplied requirements
     */
    private fun populateTable(table : Table, requirements : Collection<SdpiRequirement>)
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
            val strLocalId = req.localId
            val strLinkId = req.blockId
            val strGlobalId = req.globalId
            val level = req.level
            val type = req.type

            val strIdLink = req.makeLink()

            val cellGlobalId = createDocument(table.document)
            cellGlobalId.blocks.add(createBlock(cellGlobalId, plainContext(Contexts.PARAGRAPH), strGlobalId, mapOf("role" to "global-id")))

            val row = createTableRow(table)
            row.cells.add(createTableCell(colId, cellGlobalId))
            row.cells.add(createTableCell(colLocalId, strIdLink))
            row.cells.add(createTableCell(colLevel, level.keyword))
            row.cells.add(createTableCell(colType, type.description))

            table.body.add(row)
        }
    }
}