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
    REQUIREMENT_GROUPS("sdpi_req_group"),
    REQUIREMENT_SPECIFICATION("sdpi_req_specification"),
    MAX_OCCURRENCE("sdpi_max_occurrence"),
    VOLUME_CAPTION("sdpi_volume_caption"),

}

sealed class RequirementAttributes {
    enum class Common(val key: String) {
        // Type of requirement.
        TYPE("sdpi_req_type"),

        // Requirement level (e.g., shall, may, etc)
        LEVEL("sdpi_req_level"),

        // Identifies a specification that the requirement belongs
        // to. Used, for example, to determine the base oid for
        // assigning globally unique object ids.
        SPECIFICATION("sdpi_req_specification"),

        // Groups requirement belongs to. Comma separated list.
        GROUPS("sdpi_req_group"),

    }

    enum class UseCase(val key: String) {
        // Attribute that identifies the use case associated with a
        // USE_CASE requirement
        ID("sdpi_use_case_id")
    }

    enum class RefIcs(val key: String) {
        // The id of the reference to the standard containing the requirement
        ID("sdpi_ref_id"),

        // Section in the reference standard containing the requirement (e.g., "§6.2")
        SECTION("sdpi_ref_section"),

        // Requirement identifier in the referenced standard (e.g., "R1001")
        REQUIREMENT("sdpi_ref_req")
    }

    enum class RiskMitigation(val key: String) {
        // Type of risk (e.g., general, safety, effectiveness, etc)
        SES_TYPE("sdpi_ses_type"),

        // How requirement can be tested (e.g., inspection, interoperability, etc.).
        TESTABILITY("sdpi_ses_test")
    }
}

enum class UseCaseAttributes(val key: String) {
    // Attribute to define an id for a use case section.
    ID("sdpi_use_case_id"),

    // Attribute to define an id for a use case scenario.
    SCENARIO("sdpi_use_case_scenario")
}