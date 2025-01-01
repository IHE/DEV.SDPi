package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.Options
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader


const val BLOCK_NAME_SDPI_REQUIREMENT_RELATED = "RELATED"

/**
 * Formats related blocks inside requirement section.
 * Related blocks lists standards that are related to the containing requirement,
 * for example a requirement from another standard that informs this requirement
 * or may provide additional information that may be useful to meet the requirement.
 */
@Name(BLOCK_NAME_SDPI_REQUIREMENT_RELATED)
@Contexts(Contexts.EXAMPLE)
class RelatedBlockProcessor : BlockProcessor(BLOCK_NAME_SDPI_REQUIREMENT_RELATED)
{
    private companion object : Logging
    {
    }

    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any
    {
        //logger.info("**** Found related block with parent: ${parent.attributes["title"]}")

        // Make this block collapsible and collapsed by default.
        attributes["collapsible-option"] = ""

        attributes["name"] = BLOCK_NAME_SDPI_REQUIREMENT_RELATED
        attributes["style"] = "RELATED"
        val block = createBlock(parent, "example",
            mapOf(
                Options.ATTRIBUTES to attributes,
                ContentModel.KEY to ContentModel.COMPOUND
            ))
        block.title = "Related"

        return block
    }
}
