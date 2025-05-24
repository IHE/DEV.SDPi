package org.sdpi.asciidoc.extension

/**
 * Defines keywords for block roles.
 */
sealed class Roles {

    enum class Profile(val key: String) {
        // Profile section.
        PROFILE("profile"),

        // Identifier for the profile
        ID("profile-id"),

        PROFILE_OPTION("profile-option"),

        ID_PROFILE_OPTION("profile-option-id"),
    }

    enum class Actor(val key: String) {
        SECTION_ROLE("actor"),

        // A role that registers the section anchor (id) as an
        // alias for an actor-id.
        ALIAS("actor-alias"),

        ID("actor-id")
    }

    enum class Transaction(val key: String) {
        TRANSACTION("transaction"),

        TRANSACTION_ID("transaction-id"),

        ACTOR_ID("actor-id"),

        CONTRIBUTION("contribution"),

        ACTOR_ROLE_TABLE("actor_roles"),

    }

    enum class UseCase(val key: String) {
        // Use case section
        FEATURE("use-case"),

        // The block providing technical pre-conditions for the use case.
        // Must be inside a feature section.
        BACKGROUND("use-case-background"),

        // Block describing one scenario in the use case. Must be
        // inside a feature section.
        SCENARIO("use-case-scenario"),

        // Steps for the scenario. Must be inside a scenario section.
        STEPS("use-case-steps"),
    }

    // Roles applied to the tables where query results will get populated
    // that are created as placeholders by block macro processors.
    enum class QueryTable(val key: String) {
        // The role applied to requirement list placeholder tables.
        REQUIREMENT("requirement-table"),

        // The role applied to transaction list tables.
        TRANSACTIONS("transactions-table"),

        // The role applied to content module tables
        CONTENT_MODULE("content-module-table"),
    }

    enum class ContentModule(val key: String) {
        SECTION_ROLE("content-module"),

        // Identifier for the content module
        ID("content-module-id"),
    }

    enum class Gateway(val key: String) {
        SECTION_ROLE("gateway"),
        ID("gateway-id"),
    }

    enum class Protocol(val key: String) {
        SECTION_ROLE("protocol"),
        ID("protocol-id"),
    }
}