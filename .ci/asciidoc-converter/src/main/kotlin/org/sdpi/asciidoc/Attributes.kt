package org.sdpi.asciidoc

/**
 * Provides access to all known block attributes.
 */
class Attributes(private val attributes: MutableMap<String, Any>) {
    operator fun set(key: BlockAttribute, value: Any) {
        attributes[key.key] = value
    }

    operator fun get(key: BlockAttribute): String? = attributes[key.key]?.toString()

    fun raw() = attributes.toMap()
}