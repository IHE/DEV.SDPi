package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable

enum class GherkinStepType(val keyword: String) {
    GIVEN("given"),
    WHEN("when"),
    THEN("then"),
    AND("and"),
    OR("or"),
}

/**
 * Takes a string and converts it to a [GherkinStepType] enum.
 *
 * @param raw Raw text being shall, should or may.
 *
 * @return the [GherkinStepType] enum or null if the conversion failed.
 */
fun resolveStepType(raw: String) = GherkinStepType.entries.firstOrNull { it.keyword == raw.lowercase() }


@Serializable
data class GherkinStep(val step: GherkinStepType, val description: String)

@Serializable
data class UseCaseScenario(
    val title: String,
    val specification: List<GherkinStep>,
)

@Serializable
data class UseCaseSpecification(
    val background: List<GherkinStep>,
    val scenarios: List<UseCaseScenario>,
)

@Serializable
data class SdpiUseCase(
    val id: String,
    val title: String,
    val anchor: String,
    val specification: UseCaseSpecification,
)