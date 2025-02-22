package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.PhraseNode
import org.asciidoctor.extension.InlineMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.parseRequirementNumber

/**
 * Processes inline macro to reference a requirement.
 * https://docs.asciidoctor.org/asciidoctorj/latest/extensions/inline-macro-processor/
 *  Target: the local id of the requirement to reference.
 *  Attributes: none
 *  Output: a local link to the requirement specification.
 * Example:
 *  RefRequirement:R1001[]
 *
 */
@Name("RefRequirement")
class RequirementReferenceMacroProcessor(private val documentInfo: SdpiInformationCollector) : InlineMacroProcessor() {
    private companion object : Logging

    override fun process(parent: ContentNode, strTarget: String, attributes: MutableMap<String, Any>): PhraseNode {
        val id = parseRequirementNumber(strTarget)
        checkNotNull(id)
        {
            "$strTarget is not a valid requirement number for a requirement reference".also { logger.error { it } }
        }

        val req = documentInfo.requirements()[id]
        checkNotNull(req)
        {
            "Requirement '$strTarget' doesn't exist".also { logger.error { it } }
        }

        val strHref = "#${req.getBlockId()}"
        val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)

        return createPhraseNode(parent, "anchor", req.localId, attributes, options)
    }
}

/**
 * Processes inline macro to reference a use-case.
 * https://docs.asciidoctor.org/asciidoctorj/latest/extensions/inline-macro-processor/
 *  Target: the case-sensitive id of the use-case to reference.
 *  Attributes: none
 *  Output: a local link to the use case specification.
 * Example:
 *  RefUseCase:stad[]
 *
 */
@Name("RefUseCase")
class UseCaseReferenceMacroProcessor(private val documentInfo: SdpiInformationCollector) : InlineMacroProcessor() {
    private companion object : Logging

    override fun process(parent: ContentNode, strTarget: String, attributes: MutableMap<String, Any>): PhraseNode {
        val useCase = documentInfo.useCases()[strTarget]
        checkNotNull(useCase)
        {
            "Use case '$strTarget' doesn't exist".also { logger.error { it } }
        }

        val strHref = "#${useCase.anchor}"
        val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)

        logger.info("Found use case: $strHref, ${useCase.title}")

        return createPhraseNode(parent, "anchor", useCase.title, attributes, options)
    }

}