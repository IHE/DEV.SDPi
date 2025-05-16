package org.sdpi.asciidoc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SdpiProfileOption(
    val id: String,
    val transactionReferences: List<SdpiTransactionReference>?
)

@Serializable
class SdpiProfile(
    val profileId: String,
    val transactionReferences: List<SdpiTransactionReference>?
) {

    @Serializable
    @SerialName("actors")
    private val actorsDefined = mutableListOf<SdpiActor>()

    fun addActor(actor: SdpiActor) {
        actorsDefined.add(actor)
    }

    fun getActor(strActorId: String): SdpiActor? {
        return actorsDefined.firstOrNull { it.id == strActorId }
    }

    @Serializable
    @SerialName("options")
    val options = mutableListOf<SdpiProfileOption>()

    fun addOption(option: SdpiProfileOption) {
        options.add(option)
    }

    fun actorReferences() = actorsDefined

    fun getTransactionObligations(
        strTransactionId: String,
        contribution: Contribution,
        strProfileOption: String? = null
    ): List<ProfileContribution> {
        val refs = mutableListOf<ProfileContribution>()
        if (strProfileOption == null) {
            gatherObligations(refs, transactionReferences, strTransactionId, contribution, null)
        }
        for (po in options) {
            if (strProfileOption == null || po.id == strProfileOption) {
                gatherObligations(refs, po.transactionReferences, strTransactionId, contribution, po.id)
            }
        }

        return refs
    }

    private fun gatherObligations(
        refs: MutableList<ProfileContribution>,
        transactionReferences: List<SdpiTransactionReference>?,
        strTransactionId: String,
        contribution: Contribution,
        strOptionId: String?
    ) {
        if (transactionReferences != null) {
            for (tr in transactionReferences.filter{it.transactionId == strTransactionId}) {
                val obligation = tr.getObligation(contribution)
                if (obligation != null) {
                    refs.add(ProfileContribution(profileId, strOptionId, strTransactionId, contribution, obligation))
                }
            }
        }
    }
}

data class ProfileContribution(
    val profileId: String,
    val profileOptionId: String?,
    val transactionId: String,
    val contribution: Contribution,
    val obligation: Obligation
)