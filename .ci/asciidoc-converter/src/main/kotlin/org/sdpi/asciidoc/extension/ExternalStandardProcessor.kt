package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.ExternalStandardAttributes
import org.sdpi.asciidoc.model.ExternalStandard
import org.sdpi.asciidoc.model.loadExternalStandardRequirementsFromJson

const val BLOCK_MACRO_EXTERNAL_STANDARD_PROCESSOR = "load-standard"

@Name(BLOCK_MACRO_EXTERNAL_STANDARD_PROCESSOR)
class ExternalStandardProcessor: BlockMacroProcessor(BLOCK_MACRO_EXTERNAL_STANDARD_PROCESSOR) {
    private companion object : Logging

    val standards = mutableListOf<ExternalStandard>()

    fun getStandard(strStandard: String) : ExternalStandard? {
        return standards.find{ it.id == strStandard}
    }

    override fun process(parent: StructuralNode, strStandardFilename: String, attributes: MutableMap<String, Any>): StructuralNode? {

        val strPath = "../../asciidoc/standards/$strStandardFilename"
        val strCitation = attributes[ExternalStandardAttributes.CITATION_KEY.key]?.toString()
        checkNotNull(strCitation) {
            logger.error("Citation is required for standard $strStandardFilename")
        }
        val strId = attributes[ExternalStandardAttributes.ID.key]?.toString()
        checkNotNull(strId) {
            logger.error("Id is required for standard $strStandardFilename")
        }

        val standard = loadExternalStandardRequirementsFromJson(strPath, strId, strCitation)
        check (standards.find{it.id == strId} == null) {
            logger.error("Duplicate standard id for $strStandardFilename")
        }

        standards.add(standard)

        return null;
    }
}