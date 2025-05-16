package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable
import kotlin.coroutines.Continuation

@Serializable
data class SdpiTransaction(
    val anchor: String,
    val id: String,
    val label: String,
    val actorRoles: List<SdpiActorRole>?
) {
    fun createTransactionListLink(): String {
        // This is a hack to create a link to the transaction table in appendix B. It's
        // a hack because we make assumptions about how transaction ids are named.
        val strAnchor = "transaction_number_${id.lowercase().replace('-', '_')}"
        return "link:#$strAnchor[$id]"
    }

    fun getContributionForActor(strActorId: String): Contribution? {
        if (actorRoles == null) {
            return null
        }

        for(actor in actorRoles) {
            if (actor.actorId == strActorId) {
                return actor.contribution
            }
        }

        return null
    }
}

enum class Obligation(val keyword: String, val symbol: String) {
    REQUIRED("required", "R"),
    OPTIONAL("optional", "O")
}

fun parseObligation(strKeyword: String) =
    Obligation.entries.firstOrNull { it.keyword.lowercase() == strKeyword.lowercase() }

enum class Contribution(val keyword: String) {
    INITIATOR("initiator"),
    RESPONDER("responder"),
    RECEIVER("receiver"),
}

fun parseContribution(strKeyword: String) =
    Contribution.entries.firstOrNull { it.keyword.lowercase() == strKeyword.lowercase() }

@Serializable
data class TransactionContribution(
    val contribution: Contribution,
    val obligation: Obligation,
)

@Serializable
data class SdpiTransactionReference(
    val transactionId: String,
    val obligations: List<TransactionContribution>
) {
    fun makesContribution(contribution: Contribution): Boolean {
        return obligations.any{ it.contribution == contribution}
    }

    fun getObligation(contribution: Contribution): Obligation? {
        return obligations.firstOrNull{it.contribution == contribution}?.obligation
    }
}

data class SdpiProfileTransactionReference(
    val profileId: String,
    val profileOptionId: String?,
    val transactionReference: SdpiTransactionReference
)