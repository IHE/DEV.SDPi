package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable

@Serializable
data class SdpiActor(
    val id: String,
    val oids: List<String>,
    val label: String,
    val profile: String,
    val anchor: String,
    val requiredActorGroupings: List<ActorGrouping>?
) {
    val requirements: MutableList<Int> = mutableListOf<Int>()

    companion object {
        val ACTOR_ID_REGEX = """<<((vol1_spec_sdpi_p_)?actor_[a-zA-Z0-9_]+)(,.+)?>>""".toRegex()
        val ACTOR_REF_REGEX = """RefActor:([a-zA-Z0-9-_]+)\[]""".toRegex()
    }
}

@Serializable
data class SdpiActorRole(
    val actorId: String,
    val contribution: Contribution,
    val description: List<String>,
) {
}

@Serializable
data class ActorGrouping(
    val actorId: String,
    val optionId: String?
) {
    companion object {
        val GROUPING_REGEX = """^\s*([0-9a-zA-Z_-]+)\s*(?:\[\s*([0-9a-zA-Z_-]+)\s*])?\s*$""".toRegex()
    }
}

@Serializable
data class SdpiActorOption(
    val id: String,
    val label: String,
    val anchor: String,
    val transactionReferences: List<SdpiTransactionReference>?,
)

