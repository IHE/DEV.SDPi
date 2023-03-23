package org.sdpi.asciidoc

import kotlinx.serialization.Serializable

/**
 * Provides access to all known block attributes.
 */
@Serializable
class Attributes(private val attributes: MutableMap<String, String?>) {
    operator fun set(key: BlockAttribute, value: String) {
        attributes[key.key] = value
    }

    operator fun get(key: BlockAttribute): String? = attributes[key.key]

    fun raw() = attributes.toMap()

    companion object {
        fun create(attributes: Map<String, Any?>): Attributes =
            Attributes(HashMap(attributes.map { it.key to it.value?.toString() }.toMap()))
    }
}