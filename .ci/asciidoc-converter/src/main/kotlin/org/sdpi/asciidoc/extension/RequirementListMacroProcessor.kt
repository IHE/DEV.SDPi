package org.sdpi.asciidoc.extension

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name;
import org.sdpi.asciidoc.plainContext
import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.Options
import org.asciidoctor.ast.ContentModel
import org.sdpi.asciidoc.Attributes

const val BLOCK_MACRO_NAME_SDPI_REQUIREMENT_LIST = "sdpi_requirement_list"

@Name(BLOCK_MACRO_NAME_SDPI_REQUIREMENT_LIST)
class RequirementListMacroProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_SDPI_REQUIREMENT_LIST)
{
    private companion object : Logging {

    }

    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): Any
    {
        logger.info {"Found SDPi requirement list"}

        attributes["role"] = "requirement-list"
        //val myNode = createBlock(parent, plainContext(Contexts.PARAGRAPH), mapOf( Options.ATTRIBUTES  to attributes, ContentModel.KEY to ContentModel.COMPOUND))

        val myNode = createTable(parent)
        myNode.attributes["role"] = "requirement-list"

        val strGroup = attributes["sdpi_req_group"]
        if (strGroup != null)
        {
            myNode.attributes["sdpi_req_group"] = strGroup
        }

        return myNode
    }

}