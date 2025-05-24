package org.sdpi.asciidoc.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SdpiProfileOption @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: String,
    val anchor: String,
    val label: String,

    @SerialName("transactionReferences")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val transactionReferences: MutableList<SdpiTransactionReference> = mutableListOf(),

    @SerialName("useCaseReferences")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val useCaseReferences: MutableList<SdpiUseCaseReference> = mutableListOf(),

    @SerialName("contentModuleReferences")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val contentModuleReferences: MutableList<SdpiContentModuleRef> = mutableListOf(),

) {
    fun add(ref: SdpiTransactionReference) {
        transactionReferences.add(ref)
    }

    fun add(ref: SdpiUseCaseReference) {
        useCaseReferences.add(ref)
    }

    fun add(ref: SdpiContentModuleRef) {
        contentModuleReferences.add(ref)
    }
}

@Serializable
class SdpiProfile(
    val profileId: String,
    val anchor: String,
    val label: String,
    val transactionReferences: List<SdpiTransactionReference>?,
    val useCaseReferences: List<SdpiUseCaseReference>?,
    val contentModuleReferences: List<SdpiContentModuleRef>?
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

    /*
    fun getOption(strOptionId: String) : SdpiProfileOption {
        val existing = options.find{ it.id == strOptionId}
        if (existing != null) {
            return existing
        }
        val newOption = SdpiProfileOption(strOptionId)
        options.add(newOption)
        return newOption
    }*/

    fun findOption(strOptionId: String): SdpiProfileOption? {
        return options.find{ it.id == strOptionId}
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

