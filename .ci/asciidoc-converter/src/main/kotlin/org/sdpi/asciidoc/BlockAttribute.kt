package org.sdpi.asciidoc

/**
 * Known ASCIIDoc and SDPi attributes.
 */
enum class BlockAttribute(val key: String) {
    ID("id"),
    TITLE("title"),
    ROLE("role"),
    REQUIREMENT_LEVEL("sdpi_req_level"),
    REQUIREMENT_TYPE("sdpi_req_type"),
    REQUIREMENT_SPECIFICATION("sdpi_req_specification"),
    REQUIREMENT_GROUPS("sdpi_req_group"),
    MAX_OCCURRENCE("sdpi_max_occurrence"),
    VOLUME_CAPTION("sdpi_volume_caption"),

}
