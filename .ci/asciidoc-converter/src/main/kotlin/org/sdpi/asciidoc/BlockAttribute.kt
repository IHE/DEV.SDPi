package org.sdpi.asciidoc

/**
 * Known ASCIIDoc and SDPi attributes.
 */
enum class BlockAttribute(val key: String) {
    ID("id"),
    TITLE("title"),
    ROLE("role"),
    REQUIREMENT_LEVEL("sdpi_req_level"),
    MAX_OCCURRENCE("sdpi_max_occurrence"),
    VOLUME_CAPTION("sdpi_volume_caption")
}
