package org.sdpi.asciidoc.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SdpiProfileOption @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: String,
    val oids: List<String>,
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
    val oids: List<String>,
    val anchor: String,
    val label: String,
    val transactionReferences: List<SdpiTransactionReference>?,
    val useCaseReferences: List<SdpiUseCaseReference>?,
    val contentModuleReferences: List<SdpiContentModuleRef>?
) {
    @Serializable
    @SerialName("options")
    val options = mutableListOf<SdpiProfileOption>()

    fun addOption(option: SdpiProfileOption) {
        options.add(option)
    }

    fun findOption(strOptionId: String): SdpiProfileOption? {
        return options.find { it.id == strOptionId }
    }

    @Serializable
    @SerialName("actors")
    private val actorsDefined = mutableListOf<SdpiActor>()

    fun addActor(actor: SdpiActor) {
        actorsDefined.add(actor)
    }

    fun getActor(strActorId: String): SdpiActor? {
        return actorsDefined.firstOrNull { it.id == strActorId }
    }

    fun actorReferences() = actorsDefined

    @Serializable
    @SerialName("actor-options")
    private val actorOptions = mutableListOf<SdpiActorOption>()

    fun addActorOption(option: SdpiActorOption) {
        val existing = actorOptions.find { it.id == option.id }
        if (existing != null) {
            throw IllegalStateException("Actor option ${option.id} already exists, with label ${existing.label}")
        }
        actorOptions.add(option)
    }

    fun getTransactionObligations(
        strTransactionId: String,
        contribution: Contribution,
        strProfileOption: String? = null,
        strActorOption: String? = null,
    ): List<ProfileContribution> {
        val refs = mutableListOf<ProfileContribution>()
        if (strProfileOption == null) {
            //println("  Profile obligations:")
            gatherObligations(refs, transactionReferences, strTransactionId, contribution, null)
        }
        for (po in options) {
            //println("  Profile-option obligations:")
            val optionId = OptionId(OptionType.PROFILE, po.id, po.label, po.anchor)
            if (strProfileOption == null || po.id == strProfileOption) {
                gatherObligations(refs, po.transactionReferences, strTransactionId, contribution, optionId)
            }
        }
        for (ao in actorOptions) {
            //println("  Profile actor-option obligations:")
            val optionId = OptionId(OptionType.ACTOR, ao.id, ao.label, ao.anchor)
            if (strActorOption == null || ao.id == strActorOption) {
                gatherObligations(refs, ao.transactionReferences, strTransactionId, contribution, optionId)
            }
        }

        /*
        println("${profileId} obligations for $strTransactionId, $contribution profile-option=$strProfileOption, actor=$strActorOption")
        for (r in refs) {
            println("  ${r.profileId}, ${r.transactionId}, ${r.optionId}, ${r.obligation}")
        }*/
        return refs
    }

    fun getTransactionObligations(
        strTransactionId: String,
        strActorId: String,
        optionFilter: Pair<OptionType, String>?
    ): List<ProfileContribution> {
        val refs = mutableListOf<ProfileContribution>()

        //println("Get obligations for $strActorId to $strTransactionId:")

        if (optionFilter == null) {
            gatherObligations(refs, transactionReferences, strTransactionId, strActorId, null)
        }

        if (optionFilter == null) {
            for (po in options) {
                //println("  Profile-option obligations:")
                val optionId = OptionId(OptionType.PROFILE, po.id, po.label, po.anchor)
                gatherObligations(refs, po.transactionReferences, strTransactionId, strActorId, optionId)
            }
        } else if (optionFilter.first == OptionType.PROFILE) {
            val po = options.find { it.id == optionFilter.second }
            if (po != null) {
                val optionId = OptionId(OptionType.PROFILE, po.id, po.label, po.anchor)
                gatherObligations(refs, po.transactionReferences, strTransactionId, strActorId, optionId)
            }
        }

        for (ao in actorOptions) {
            //println("  Profile actor-option obligations:")
            val optionId = OptionId(OptionType.ACTOR, ao.id, ao.label, ao.anchor)
            gatherObligations(refs, ao.transactionReferences, strTransactionId, strActorId, optionId)
        }

        return refs
    }

    private fun gatherObligations(
        refs: MutableList<ProfileContribution>,
        transactionReferences: List<SdpiTransactionReference>?,
        strTransactionId: String,
        contribution: Contribution,
        optionId: OptionId?
    ) {
        if (transactionReferences != null) {
            for (tr in transactionReferences.filter { it.transactionId == strTransactionId }) {
                val obligation = tr.getObligation(contribution)
                if (obligation != null) {
                    val pc = ProfileContribution(profileId, optionId, strTransactionId, contribution, obligation)
                    //println("    $profileId, $optionId, $strTransactionId, $contribution, $obligation")
                    refs.add(pc)
                }
            }
        }
    }

    private fun gatherObligations(
        refs: MutableList<ProfileContribution>,
        transactionReferences: List<SdpiTransactionReference>?,
        strTransactionId: String,
        strActorId: String,
        optionId: OptionId?
    ) {
        if (transactionReferences != null) {
            for (tr in transactionReferences.filter { it.transactionId == strTransactionId }) {
                val obligations = tr.obligations.filter { it.actorId == strActorId }
                for (obl in obligations) {
                    val pc =
                        ProfileContribution(profileId, optionId, strTransactionId, obl.contribution, obl.obligation)
                    //println("    $profileId, $strTransactionId,$strActorId,  $obl")
                    refs.add(pc)
                }
            }
        }

    }
}

enum class OptionType { PROFILE, ACTOR }

data class OptionId(
    val type: OptionType,
    val strId: String,
    val label: String,
    val anchor: String,
)

data class ProfileContribution(
    val profileId: String,
    val optionId: OptionId?,
    val transactionId: String,
    val contribution: Contribution,
    val obligation: Obligation
)

