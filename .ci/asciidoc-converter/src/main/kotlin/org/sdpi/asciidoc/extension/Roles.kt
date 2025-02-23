package org.sdpi.asciidoc.extension

/**
 * Defines keywords for block roles.
 */
sealed class RoleNames {
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
}