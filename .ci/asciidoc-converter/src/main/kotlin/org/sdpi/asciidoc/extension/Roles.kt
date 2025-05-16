package org.sdpi.asciidoc.extension

/**
 * Defines keywords for block roles.
 */
sealed class RoleNames {

    enum class Profile(val key: String) {
        // Profile section.
        PROFILE("profile"),

        // Identifier for the profile
        ID("profile-id"),

        PROFILE_OPTION("profile-option"),

        ID_PROFILE_OPTION("profile-option-id"),
    }

    enum class Transaction(val key: String) {
        TRANSACTION("transaction"),

        TRANSACTION_ID("transaction-id"),

        ACTOR_ID("actor-id"),

        CONTRIBUTION("contribution"),

        ACTOR_ROLE_TABLE("actor_roles"),

        OPTION("option"),
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
        TRANSACTIONS("transactions-table")
    }

    enum class ContentModule(val key: String) {
        SECTION_ROLE("content-module"),
    }

    enum class Gateway(val key: String) {
        SECTION_ROLE("gateway"),
    }

    enum class Protocol(val key: String) {
        SECTION_ROLE("protocol"),
    }
}