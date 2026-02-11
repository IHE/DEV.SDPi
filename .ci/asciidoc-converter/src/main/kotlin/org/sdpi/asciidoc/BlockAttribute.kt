package org.sdpi.asciidoc

/**
 * Known ASCIIDoc and SDPi attributes.
 */
enum class BlockAttribute(val key: String) {
    ID("id"),
    TITLE("title"),
    ROLE("role"),

    // Optional value that may be attached to requirements.
    MAX_OCCURRENCE("sdpi_max_occurrence"),

    VOLUME_CAPTION("sdpi_volume_caption"),

    // The leaf-arc of the item's oid. The complete oid is computed
    // by appending this to the root based on the block type when
    // the attribute starts with a "."  (e.g.,
    // 1.3.6.1.4.1.19376.1.6.3 for DEV actors, see
    // https://wiki.ihe.net/index.php/PCD_OID_Management). If the
    // value doesn't start with a '.' we use it directly.
    LEAF_ARC("oid-arcs")
}

/**
 * Defines attribute keywords for sdpi_requirement blocks
 */
sealed class RequirementAttributes {
    enum class Common(val key: String) {
        // Type of requirement.
        TYPE("sdpi_req_type"),

        // Requirement level (e.g., shall, may, etc)
        LEVEL("sdpi_req_level"),

        // Groups requirement belongs to. Comma separated list.
        GROUPS("sdpi_req_group"),

        // Requirements referencing an actor.
        ACTOR("sdpi_req_actor"),

    }

    enum class UseCase(val key: String) {
        // Attribute that identifies the use case associated with a
        // USE_CASE requirement
        ID("use-case-id")
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
    ID("use-case-id"),

    // Attribute to define an id for a use case scenario.
    SCENARIO("sdpi_use_case_scenario"),

    ACTOR("actor-id"),

    OBLIGATION("support"),


}

enum class ContentModuleAttributes(val key: String) {
    ACTOR("actor-id"),
    OBLIGATION("support"),

    // Content module is not defined yet, but deferred to a future version.
    // This attribute gives the name of the placeholder.
    PLACEHOLDER_NAME("placeholder-name")
}

enum class TransactionIncludeAttributes(val key: String) {
    ACTOR("actor-id"),

    // Transaction is not defined yet, but deferred to a future version.
    // This attribute gives the name of the placeholder.
    PLACEHOLDER_NAME("placeholder-name")
}

sealed class TableAttributes(val key: String) {

    enum class OidTable(val key: String) {
        // Define filter for the root arc for oids to include in the table.
        ROOT_ARC("arc")
    }

    enum class IcsTable(val key:String) {
        // When defined, the ICS table is populated from an
        // imported external standard
        SOURCE_STANDARD("standard-id")
    }
}

enum class ExternalStandardAttributes(val key: String) {
    // Attribute with bibliography entry for the standard [required]
    CITATION_KEY("cite"),

    // An document-unique identifier to reference the standard in ICS tables and requirement references.
    ID("id"),

    // ID of the standard when referencing requirements with the "RefRequirement" inline macro.
    STANDARD_ID("standard-id")
}