package org.sdpi.asciidoc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

    fun actorReferences() = actorsDefined
}