package org.sdpi.asciidoc.model
import kotlinx.serialization.Serializable

@Serializable
data class BibliographyEntry(
    val reference: String,
    val referenceText: String,
    val source: String
)
