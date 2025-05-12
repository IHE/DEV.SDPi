package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable

@Serializable
data class SdpiActor(
    val id: String,
    val label: String,
    val profile: String,
) {
    val requirements: MutableList<Int> = mutableListOf<Int>()

    companion object {
        val ACTOR_ID_REGEX = """<<(vol1_spec_sdpi_p_actor_[a-zA-Z0-9_]+)(,.+)?>>""".toRegex()
    }
}

@Serializable
data class SdpiActorRole(
    val actorId: String,
    val contribution: Contribution,
    val optionId: String?,
    val description: List<String>,
) {
}

