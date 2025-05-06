package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable

@Serializable
data class SdpiActor(
    val id: String,
    val label: String,
    val profile: String,
) {
}

@Serializable
data class SdpiActorRole(
    val actorId: String,
    val contribution: Contribution,
    val optionId: String?,
    val description: List<String>,
)

