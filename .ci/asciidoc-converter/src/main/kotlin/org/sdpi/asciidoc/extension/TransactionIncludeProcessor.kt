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
    private val transactionReferences = mutableMapOf<String, MutableList<SdpiProfileTransactionReference>>()

    //private val reMatchObligation = Regex("""(\w+)(?:\(([^)]+)\))?""")

    fun profiles() = transactionReferences

    fun transactionReferences(strProfile: String): List<SdpiProfileTransactionReference>? {
        return transactionReferences[strProfile]
    }

    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): Any? {
        val (strProfileId, strProfileOptionId) = findProfileId(parent)
        checkNotNull(strProfileId) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_TRANSACTION requires a ancestor block within the 'profile' role")
        }

        val obligations: MutableList<TransactionContribution> = mutableListOf()
        for (attr in attributes) {
            val transactionContribution = parseContribution(attr.key, attr.value.toString())
            obligations.add(transactionContribution)
        }

        val transactionReference = SdpiTransactionReference(target, obligations)
        val profileReference = SdpiProfileTransactionReference(strProfileId, strProfileOptionId, transactionReference)

        addReference(profileReference)

        return null
    }

    private fun parseContribution(strContribution: String, strObligation: String): TransactionContribution {
        val contribution = parseContribution(strContribution)
        checkNotNull(contribution) {
            logger.error("Unknown contribution $strContribution")
        }

        val obligation = parseObligation(strObligation)
        checkNotNull(obligation) {
            logger.error("Unknown obligation $strObligation")
        }

        return TransactionContribution(contribution, obligation)
    }

    private fun addReference(ref: SdpiProfileTransactionReference) {
        var profileTransactions = transactionReferences[ref.profileId]
        if (profileTransactions == null) {
            profileTransactions = mutableListOf<SdpiProfileTransactionReference>()
            transactionReferences[ref.profileId] = profileTransactions
        } else {
            val bDuplicate = profileTransactions.any {
                (it.transactionReference.transactionId == ref.transactionReference.transactionId)
                        && (it.profileOptionId == ref.profileOptionId)
            }
            check(!bDuplicate) {
                logger.error("Profile ${ref.profileId} (option=${ref.profileOptionId}) already references ${ref.transactionReference.transactionId}")
            }
        }
        profileTransactions.add(ref)

    }

    /*    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): Any? {
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


    private fun parseObligations(strObligationSet: String): Pair<Obligation, List<SdpiProfileTransaction>?> {
        val optionalObligations = mutableListOf<SdpiProfileTransaction>()

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
*/
    fun dump() {
        for (profile in transactionReferences) {
            logger.info("Profile: ${profile.key}")

            for (ref in profile.value) {
                if (ref.profileOptionId != null) {
                    logger.info("  * ${ref.transactionReference.transactionId} (${ref.profileOptionId})")
                } else {
                    logger.info("  * ${ref.transactionReference.transactionId}")
                }
            }
        }
    }

    private fun findProfileId(parent: ContentNode): Pair<String?, String?> {
        logger.info("Find profile id, parent = ${parent.id}")
        if (parent.hasRole(RoleNames.Profile.PROFILE.key)) {
            val strProfileId = parent.attributes[RoleNames.Profile.ID.key]?.toString()
            checkNotNull(strProfileId) {
                logger.error("Profile has no id")
            }
            return Pair(strProfileId, null)
        } else if (parent.hasRole(RoleNames.Profile.PROFILE_OPTION.key)) {
            val strOptionId = parent.attributes[RoleNames.Profile.ID_PROFILE_OPTION.key]?.toString()
            val parentId = findProfileId(parent.parent)
            return Pair(parentId.first, strOptionId)
        } else if (parent.parent != parent.document) {
            return findProfileId(parent.parent)
        } else {
            return Pair(null, null)
        }
    }
}