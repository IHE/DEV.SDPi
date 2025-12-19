package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.PhraseNode
import org.asciidoctor.extension.InlineMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.ExternalStandardAttributes
import org.sdpi.asciidoc.LinkStyles
import org.sdpi.asciidoc.parseRequirementNumber

/**
 * Processes inline macro to reference a requirement.
 * https://docs.asciidoctor.org/asciidoctorj/latest/extensions/inline-macro-processor/
 *  Target: the local id of the requirement to reference.
 *  Attributes:
 *      - standard-id [optional]: id of standard included with load-standard
 *  Output: a local link to the requirement specification.
 * Example:
 *  RefRequirement:R1001[]
 *  RefRequirement:R0121[standard-id=10207]
 */
@Name("RefRequirement")
class RequirementReferenceMacroProcessor(private val documentInfo: SdpiInformationCollector,
                                         private val externalStandardsProcessor : ExternalStandardProcessor,
                                         private val bibliographyCollector : BibliographyCollector)
    : InlineMacroProcessor() {

        private companion object : Logging

    override fun process(parent: ContentNode, strTarget: String, attributes: MutableMap<String, Any>): PhraseNode {
        val strStandardId = attributes[ExternalStandardAttributes.STANDARD_ID.key]?.toString()
        if (null == strStandardId) {
            // Reference to a requirement defined locally in the supplement
            val nRequirementId = parseRequirementNumber(strTarget)
            checkNotNull(nRequirementId) {
                "$strTarget is not a valid requirement number for a requirement reference".also { logger.error { it } }
            }

            val req = documentInfo.requirements()[nRequirementId]
            checkNotNull(req) {
                "Requirement '$strTarget' ($nRequirementId) doesn't exist".also { logger.error { it } }
            }

            val strHref = "#${req.getBlockId()}"
            val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)

            return createPhraseNode(parent, "anchor", req.localId, attributes, options)

        } else {
            // Referencing a requirement defined in an included standard.
            val standard = externalStandardsProcessor.getStandard(strStandardId)
            checkNotNull(standard) {
                "Standard '$strStandardId' doesn't exist".also { logger.error { it } }
            }

            val citation = bibliographyCollector.findEntry(standard.citationKey)
            checkNotNull(citation) {
                "Bibliography doesn't included a reference for key '${standard.citationKey}'".also { logger.error { it } }
            }

            val req = standard.getRequirement(strTarget)
            checkNotNull(req) {
                "Standard '$strStandardId' doesn't include requirement '$strTarget'".also { logger.error { it } }
            }

            val strLinkText = "$strTarget in [${citation.referenceText}]"
            val strHref = "#${standard.citationKey}"
            val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)

            return createPhraseNode(parent, "anchor", strLinkText, attributes, options)
        }
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
        checkNotNull(useCase) {
            "Use case '$strTarget' doesn't exist".also { logger.error { it } }
        }

        val strHref = "#${useCase.anchor}"
        val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)

        logger.info("Found use case: $strHref, ${useCase.title}")

        return createPhraseNode(parent, "anchor", useCase.title, attributes, options)
    }

}

@Name("RefActor")
class ActorReferenceMacroProcessor(private val documentInfo: SdpiInformationCollector) : InlineMacroProcessor() {
    private companion object : Logging

    override fun process(parent: ContentNode, strTarget: String, attributes: MutableMap<String, Any>): PhraseNode {
        val actor = documentInfo.findActor(strTarget)
        checkNotNull(actor) {
            "Actor '$strTarget' doesn't exist".also { logger.error { it } }
        }

        val strHref = "#${actor.anchor}"
        val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)
        attributes["role"] = LinkStyles.TITLE_TEXT.className

        //logger.info("Found actor: $strHref, ${actor.label}")

        return createPhraseNode(parent, "anchor", actor.label, attributes, options)
    }
}

@Name("RefContentModule")
class ContentModuleReferenceMacroProcessor(private val documentInfo: SdpiInformationCollector) :
    InlineMacroProcessor() {
    private companion object : Logging

    override fun process(parent: ContentNode, strTarget: String, attributes: MutableMap<String, Any>): PhraseNode {
        val contentModule = documentInfo.contentModules()[strTarget]
        checkNotNull(contentModule) {
            "Content module '$strTarget' doesn't exist".also { logger.error { it } }
        }

        val strHref = "#${contentModule.anchor}"
        val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)

        //logger.info("Found content-module: $strHref, ${contentModule.label}")

        return createPhraseNode(parent, "anchor", contentModule.label, attributes, options)
    }
}

@Name("RefTransaction")
class TransactionReferenceMacroProcessor(private val documentInfo: SdpiInformationCollector) : InlineMacroProcessor() {
    private companion object : Logging

    override fun process(parent: ContentNode, strTarget: String, attributes: MutableMap<String, Any>): PhraseNode {
        val transaction = documentInfo.transactions()[strTarget]
        checkNotNull(transaction) {
            "Transaction '$strTarget' doesn't exist".also { logger.error { it } }
        }

        val strHref = "#${transaction.anchor}"
        val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)
        attributes["role"] = LinkStyles.TITLE_TEXT.className

        //logger.info("Found transaction: $strHref, ${transaction.label}")

        return createPhraseNode(parent, "anchor", transaction.label, attributes, options)
    }
}

@Name("RefProfile")
class ProfileReferenceMacroProcessor(private val documentInfo: SdpiInformationCollector) : InlineMacroProcessor() {
    private companion object : Logging

    override fun process(parent: ContentNode, strTarget: String, attributes: MutableMap<String, Any>): PhraseNode {
        val profile = documentInfo.getProfile(strTarget)
        checkNotNull(profile) {
            "Profile '$strTarget' doesn't exist".also { logger.error { it } }
        }

        val strOption = attributes[Roles.Profile.ID_PROFILE_OPTION.key]?.toString()
        if (strOption == null) {

            val strHref = "#${profile.anchor}"
            val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)
            attributes["role"] = LinkStyles.TITLE_TEXT.className

            //logger.info("Found profile: $strHref, ${profile.label}")

            return createPhraseNode(parent, "anchor", profile.label, attributes, options)
        } else {
            val profileOption = profile.findOption(strOption)
            checkNotNull(profileOption) {
                "Profile $strTarget does not have a $strOption option"
            }

            val strHref = "#${profileOption.anchor}"
            val options: Map<String, String> = mapOf("type" to ":link", "target" to strHref)
            attributes["role"] = LinkStyles.TITLE_TEXT.className

            //logger.info("Found profile option: $strHref, ${profileOption.label}")

            return createPhraseNode(parent, "anchor", profileOption.label, attributes, options)
        }
    }
}
