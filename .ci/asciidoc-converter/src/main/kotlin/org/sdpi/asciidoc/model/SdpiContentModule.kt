package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable

@Serializable
data class SdpiContentModule(
    val id: String,
    val label: String,
    val anchor: String
) {
}

@Serializable
data class SdpiContentModuleRef(
    val contentModuleId: String,
    val actorId: String,
    val obligation: Obligation,
    val placeholderName: String? // Name of content-module iff the reference is deferred.
)

data class SdpiProfileContentModuleRef(
    val profileId: String,
    val profileOptionId: String?,
    val ref: SdpiContentModuleRef
)