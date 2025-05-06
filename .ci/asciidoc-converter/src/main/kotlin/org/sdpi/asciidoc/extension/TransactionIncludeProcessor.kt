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

    // Keyed by profile id (e.g., "SDPi-P"), this maps profiles to transactions.
    private val profiles = mutableMapOf<String, MutableList<SdpiTransactionReference>>()

    private val reMatchObligation = Regex("""(\w+)(?:\(([^)]+)\))?""")

    fun profiles() = profiles

    fun transactionReferences(strProfile: String): List<SdpiTransactionReference>? {
        return profiles[strProfile]
    }

    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): Any? {
        val strProfileId = findProfileId(parent)
        checkNotNull(strProfileId) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_TRANSACTION requires a ancestor block within the 'profile' role")
        }

        val obligations: MutableList<TransactionContribution> = mutableListOf()
        for (attr in attributes) {
            val transactionContribution = parseContribution(attr.key, attr.value.toString())
            obligations.add(transactionContribution)
        }

        val transactionReference = createTransactionReference(target, attributes)

        if (!profiles.containsKey(strProfileId)) {
            profiles[strProfileId] = mutableListOf()
        }
        profiles[strProfileId]?.add(transactionReference)
        return null
    }

    private fun createTransactionReference(strTransactionId: String, attributes: MutableMap<String, Any>): SdpiTransactionReference {
        val obligations: MutableList<TransactionContribution> = mutableListOf()
        for (attr in attributes) {
            val transactionContribution = parseContribution(attr.key, attr.value.toString())
            obligations.add(transactionContribution)
        }

        return SdpiTransactionReference(strTransactionId, obligations)
    }

    private fun parseContribution(strContribution: String, strObligationSet: String): TransactionContribution {
        val contribution = parseContribution(strContribution)
        checkNotNull(contribution) {
            logger.error("Unknown contribution $strContribution")
        }

        val (defaultObligation, obligationOptions) = parseObligations(strObligationSet)
        return TransactionContribution(contribution, defaultObligation, obligationOptions)
    }

    private fun parseObligations(strObligationSet: String): Pair<Obligation, List<TransactionContributionOption>?> {
        val optionalObligations = mutableListOf<TransactionContributionOption>()

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
        return Pair(defaultObligation, optOb)
    }

    fun dump() {
        for (profile in profiles) {
            logger.info("Profile: ${profile.key}")

            for (ref in profile.value) {
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