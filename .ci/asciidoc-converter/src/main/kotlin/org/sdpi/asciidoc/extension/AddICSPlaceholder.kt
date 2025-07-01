package org.sdpi.asciidoc.extension

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.RequirementAttributes

const val BLOCK_MACRO_NAME_SDPI_ICS_TABLE = "sdpi_ics_table"

/**
 * The role applied to implementation conformance statement tables
 * This role enables the ICS table populator to figure out which tables it
 * needs to populate.
 */
const val ICS_TABLE_ROLE = "ics-table"

/**
 * Processor for ics table block macro.
 * The ics table block macro provides a mechanism to insert a table
 * summarizing requirements for conformance to the profile. For
 * example, all requirement in a group.
 *
 * Filter parameters are included as macro attributes. Available filters are:
 *      - groups (sdpi_req_group): include requirements belonging to any
 *              of the supplied groups.
 *
 * Example: to include a table of all requirements in the consumer or
 * discovery-proxy groups insert:
 * sdpi_ics_table::[sdpi_req_group="consumer,discovery-proxy"]
 *
 * Not all requirements may be defined when the block macro is processed so
 * this processor just inserts a placeholder for the ICS table to be populated
 * later.
 */
@Name(BLOCK_MACRO_NAME_SDPI_ICS_TABLE)
class AddICSPlaceholder : BlockMacroProcessor(BLOCK_MACRO_NAME_SDPI_ICS_TABLE) {

    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): StructuralNode {
        attributes["role"] = ICS_TABLE_ROLE
        val placeholderTable = createTable(parent)
        placeholderTable.attributes["role"] = ICS_TABLE_ROLE

        // Add filter attributes to the table for the tree processor to consume.
        val strGroup = attributes[RequirementAttributes.Common.GROUPS.key]
        if (strGroup != null) {
            placeholderTable.attributes[RequirementAttributes.Common.GROUPS.key] = strGroup
        }

        return placeholderTable
    }

}