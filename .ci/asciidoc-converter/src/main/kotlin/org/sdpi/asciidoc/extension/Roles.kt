package org.sdpi.asciidoc.extension

/**
 * Defines keywords for block roles.
 */
sealed class Roles {

    /*
    Document section defining a profile.
     */
    enum class Profile(val key: String) {
        // Profile section.
        PROFILE("profile"),

        // Identifier for the profile
        ID("profile-id"),

        // Profile subsection defining a profile actor option
        // e.g., discovery proxy.
        PROFILE_OPTION("profile-option"),

        // Identifier for the profile actor option.
        ID_PROFILE_OPTION("profile-option-id"),
    }

    // Section defining an actor.
    enum class Actor(val key: String) {
        SECTION_ROLE("actor"),

        // A role that registers the section anchor (id) as an
        // alias for an actor-id.
        ALIAS("actor-alias"),

        ID("actor-id"),

        // A role defining an option for one or more actors.
        OPTION("actor-option"),

        // Id for the actor option
        OPTION_ID("actor-option-id"),

        // List of actors an actor is grouped with.
        GROUPING("actor-grouping"),
    }

    // Section defining a transaction.
    enum class Transaction(val key: String) {
        TRANSACTION("transaction"),

        TRANSACTION_ID("transaction-id"),

        ACTOR_ID("actor-id"),

        CONTRIBUTION("contribution"),

        ACTOR_ROLE_TABLE("actor_roles"),

    }

    // Section defining a use case.
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

    /*
    Profile section containing obligations of actors in the profile
    to support one referenced use-case.
     */
    enum class UseCaseSupport(val key: String) {

        // Role id.
        SECTION_ROLE("support-use-case"),

        // Id of the use case the section applies to.
        USE_CASE_ID("use-case-id"),

        // Type of support an actor is obliged to supply for the use case.
        OBLIGATION("support"),

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

        // The role applied to oid tables
        OID("oid-table"),
    }

    // Section defining a content module.
    enum class ContentModule(val key: String) {
        SECTION_ROLE("content-module"),

        // Identifier for the content module
        ID("content-module-id"),
    }

    // Section defining a gateway.
    enum class Gateway(val key: String) {
        SECTION_ROLE("gateway"),
        ID("gateway-id"),
    }

    // Section defining a protocol.
    enum class Protocol(val key: String) {
        SECTION_ROLE("protocol"),
        ID("protocol-id"),
    }
}