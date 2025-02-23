package org.sdpi.asciidoc.extension

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.apache.logging.log4j.kotlin.Logging
import org.sdpi.asciidoc.BlockAttribute
import org.sdpi.asciidoc.RequirementAttributes

const val BLOCK_MACRO_NAME_SDPI_REQUIREMENT_TABLE = "sdpi_requirement_table"

/**
 * The role applied to requirement list placeholder tables. This role
 * enables the RequirementListProcessor to figure out which tables it
 * needs to populate.
 */
const val REQUIREMENT_TABLE_ROLE = "requirement-table"

/**
 * Processor for requirement table block macro.
 * The requirement table block macro provides a mechanism to insert a table
 * summarizing requirements that match a filter included in the macro. For
 * example, all requirement in a group.
 *
 * Filter parameters are included as macro attributes. Available filters are:
 *      - groups (sdpi_req_group): include requirements belonging to any
 *              of the supplied groups.
 *
 * Example: to include a table of all requirements in the consumer or
 * discovery-proxy groups insert:
 * sdpi_requirement_table::[sdpi_req_group="consumer,discovery-proxy"]
 *
 * Not all requirements may be defined when the block macro is processed so
 * this processor prepares the document for the PopulateTables
 * to actually populate the table; it simply inserts a table placeholder.
 */
@Name(BLOCK_MACRO_NAME_SDPI_REQUIREMENT_TABLE)
class AddRequirementQueryPlaceholder : BlockMacroProcessor(BLOCK_MACRO_NAME_SDPI_REQUIREMENT_TABLE) {
    private companion object : Logging;

    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): Any {
        attributes["role"] = REQUIREMENT_TABLE_ROLE
        val placeholderTable = createTable(parent)
        placeholderTable.attributes["role"] = REQUIREMENT_TABLE_ROLE

        // Add filter attributes to the table for the tree processor to consume.
        val strGroup = attributes[RequirementAttributes.Common.GROUPS.key]
        if (strGroup != null) {
            placeholderTable.attributes[RequirementAttributes.Common.GROUPS.key] = strGroup
        }

        return placeholderTable
    }

}