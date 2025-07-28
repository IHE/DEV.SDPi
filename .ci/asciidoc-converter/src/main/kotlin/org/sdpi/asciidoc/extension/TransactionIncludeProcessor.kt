package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.TransactionIncludeAttributes
import org.sdpi.asciidoc.findProfileId
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

    override fun process(parent: StructuralNode, strTransactionId: String, attributes: MutableMap<String, Any>): Any? {
        val (strProfileId, strProfileOptionId, strActorOptionId) = findContextId(parent)
        checkNotNull(strProfileId) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_TRANSACTION requires a ancestor block within the 'profile' role")
        }

        val strPlaceholderName = attributes[TransactionIncludeAttributes.PLACEHOLDER_NAME.key]?.toString()


        // logger.info("Processing transaction include for $strTransactionId in $strProfileId/$strProfileOptionId/$strActorOptionId")
        val ref = transactionReference(
            strProfileId,
            strProfileOptionId,
            strActorOptionId,
            strTransactionId,
            strPlaceholderName
        )

        val strActor = attributes[TransactionIncludeAttributes.ACTOR.key]?.toString()
        for (attr in attributes) {
            if (attr.key != TransactionIncludeAttributes.ACTOR.key
                && attr.key != TransactionIncludeAttributes.PLACEHOLDER_NAME.key) {
                val transactionContribution = parseContribution(strActor, attr.key, attr.value.toString())
                ref.transactionReference.addObligation(transactionContribution)
            }
        }

        return null
    }

    // Searches up the document tree to find the context for a transaction. Context could
    // be a profile, profile-option or actor option.
    fun findContextId(parent: ContentNode): Triple<String?, String?, String?> {
        if (parent.hasRole(Roles.Profile.PROFILE.key)) {
            val strProfileId = parent.attributes[Roles.Profile.ID.key]?.toString()
            checkNotNull(strProfileId) {
                throw IllegalStateException("Profile has no id")
            }
            return Triple(strProfileId, null, null)
        } else if (parent.hasRole(Roles.Profile.PROFILE_OPTION.key)) {
            val strOptionId = parent.attributes[Roles.Profile.ID_PROFILE_OPTION.key]?.toString()
            val parentId = findProfileId(parent.parent)
            return Triple(parentId.first, strOptionId, null)
        } else if (parent.hasRole(Roles.Actor.OPTION.key)) {
            val strOptionId = parent.attributes[Roles.Actor.OPTION_ID.key]?.toString()
            val parentId = findProfileId(parent.parent)
            return Triple(parentId.first, null, strOptionId)
        } else if (parent.parent != parent.document) {
            return findContextId(parent.parent)
        } else {
            return Triple(null, null, null)
        }
    }


    private fun transactionReference(
        strProfileId: String,
        strProfileOptionId: String?,
        strActorOptionId: String?,
        strTransactionId: String,
        strDeferredName: String?
    ): SdpiProfileTransactionReference {
        val profileReferences = getProfileReferences(strProfileId)
        val existing = profileReferences.find {
            it.profileOptionId == strProfileOptionId
                    && it.actorOptionId == strActorOptionId
                    && it.transactionReference.transactionId == strTransactionId
        }
        if (existing != null) {
            return existing
        }
        val newRef = SdpiProfileTransactionReference(
            strProfileId,
            strProfileOptionId,
            strActorOptionId,
            SdpiTransactionReference(strTransactionId, strDeferredName)
        )
        profileReferences.add(newRef)
        return newRef
    }

    private fun getProfileReferences(strProfileId: String): MutableList<SdpiProfileTransactionReference> {
        val ref = transactionReferences[strProfileId]
        if (ref != null) {
            return ref
        }
        val newRefs = mutableListOf<SdpiProfileTransactionReference>()
        transactionReferences[strProfileId] = newRefs
        return newRefs
    }

    private fun parseContribution(
        strActor: String?,
        strContribution: String,
        strObligation: String
    ): TransactionContribution {
        val contribution = parseContribution(strContribution)
        checkNotNull(contribution) {
            logger.error("Unknown contribution $strContribution")
        }

        val obligation = parseObligation(strObligation)
        checkNotNull(obligation) {
            logger.error("Unknown obligation $strObligation")
        }

        //logger.info("For {$strActor} $contribution => $obligation")

        return TransactionContribution(contribution, obligation, strActor)
    }

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


}