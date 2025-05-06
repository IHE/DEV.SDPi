package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.model.*

const val BLOCK_MACRO_NAME_INCLUDE_TRANSACTION = "sdpi_include_transaction"

@Name(BLOCK_MACRO_NAME_INCLUDE_TRANSACTION)
class TransactionIncludeProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_INCLUDE_TRANSACTION) {

    private companion object : Logging

    private val profiles = mutableMapOf<String, SdpiProfile>()

    fun profiles() = profiles

    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): Any? {
        val obligations: MutableList<TransactionContribution> = mutableListOf()
        val reMatchObligation = Regex("""(\w+)(?:\(([^)]+)\))?""")

        for (attr in attributes) {
            val contribution = parseContribution(attr.key)
            checkNotNull(contribution) {
                logger.error("Unknown contribution ${attr.key}")
            }
            val optionalObligations = mutableListOf<TransactionContributionOption>()
            val strObligationSet = attr.value.toString()
            var defaultObligation = Obligation.REQUIRED
            for (strObligationTerm in strObligationSet.split('|')) {
                val matchedObligation = reMatchObligation.matchEntire(strObligationTerm)
                if (matchedObligation != null) {
                    val (strObligation, strOptionId) = matchedObligation.destructured
                    val newObligation = parseObligation(strObligation)
                    checkNotNull(newObligation) {
                        logger.error("Unknown obligation $strObligation")
                    }
                    if (strOptionId.isEmpty()) {
                        defaultObligation = newObligation
                    } else {
                        optionalObligations.add(TransactionContributionOption(strOptionId, newObligation))
                    }

                }
            }

            val optOb = if (optionalObligations.isEmpty()) null else optionalObligations
            val transactionContribution = TransactionContribution(contribution, defaultObligation, optOb)
            obligations.add(transactionContribution)
        }

        val reference = SdpiTransactionReference(target, obligations)
        val strProfileId = findProfileId(parent)
        checkNotNull(strProfileId){
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_TRANSACTION requires a ancestor block within the 'profile' role")
        }

        if (!profiles.containsKey(strProfileId)) {
            profiles[strProfileId] = SdpiProfile(strProfileId)
        }
        profiles[strProfileId]?.addTransactionReference(reference)
        return null
    }

    fun dump() {
        for (profile in profiles) {
            logger.info("Profile: ${profile.key}")

            for (ref in profile.value.referencedTransactions()) {
                logger.info("  * ${ref.transactionId}")
            }
        }
    }

    private fun findProfileId(parent: ContentNode): String? {
        if (parent.hasRole(RoleNames.Profile.PROFILE.key)) {
            val strProfileId = parent.attributes[RoleNames.Profile.ID.key]?.toString()
            checkNotNull(strProfileId) {
                logger.error("Profile has no id")
            }
            return strProfileId
        } else if (parent.parent != parent.document) {
            return findProfileId(parent.parent)
        } else {
            return null
        }
    }
}