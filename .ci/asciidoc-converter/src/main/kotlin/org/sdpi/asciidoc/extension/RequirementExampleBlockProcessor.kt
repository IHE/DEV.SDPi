package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.Options
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader

const val BLOCK_NAME_SDPI_REQUIREMENT_EXAMPLE = "EXAMPLE"

/**
 * Formats example blocks inside a requirement section.
 * Example blocks provide examples that may be useful to meet the requirement.
 */
@Name(BLOCK_NAME_SDPI_REQUIREMENT_EXAMPLE)
@Contexts(Contexts.EXAMPLE)
class RequirementExampleBlockProcessor : BlockProcessor(BLOCK_NAME_SDPI_REQUIREMENT_EXAMPLE) {
    private companion object : Logging;

    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any {
        //logger.info("**** Found example block with parent: ${parent.attributes["title"]}")

        // Make this block collapsible and collapsed by default.
        attributes["collapsible-option"] = ""

        attributes["name"] = BLOCK_NAME_SDPI_REQUIREMENT_EXAMPLE
        attributes["style"] = "EXAMPLE"


        val block = createBlock(
            parent, "example",
            mapOf(
                Options.ATTRIBUTES to attributes,
                ContentModel.KEY to ContentModel.COMPOUND
            )
        )
        block.title = "Example"

        return block
    }
}
