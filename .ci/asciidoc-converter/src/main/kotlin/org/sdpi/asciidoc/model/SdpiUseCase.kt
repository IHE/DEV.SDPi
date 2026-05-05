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

/*
Stores the information that makes up one step in a use case scenario or
background requirement.
 */
@Serializable
data class GherkinStep(val step: GherkinStepType, val description: String)

/*
Stores the steps for a use case scenario.
 */
@Serializable
data class UseCaseScenario(
    val oids: List<String>,
    val title: String,
    val specification: List<GherkinStep>,
)

/*
Stores the steps and background to specify a use case.
 */
@Serializable
data class UseCaseSpecification(
    val background: List<GherkinStep>,
    val scenarios: List<UseCaseScenario>,
)

/*
Stores the definition of a use case.
 */
@Serializable
data class SdpiUseCase(
    val id: String,
    val oids: List<String>,
    val title: String,
    val anchor: String,
    val specification: UseCaseSpecification,
)

/*
Stores the obligation of one actor to a use case, optionally
filtered by a profile option.
 */
@Serializable
data class UseCaseObligations(
    val actorId: String,
    val obligation: Obligation,
    val profileOptionId: String?,
)

/*
Stores obligations of profile actors to referenced use cases.
 */
@Serializable
data class SdpiUseCaseSupport(
    val useCaseId: String,
    val oid: List<String>,
    val anchor: String,
    val obligations: List<UseCaseObligations>
)

@Serializable
data class SdpiUseCaseReference(
    val useCaseId: String,
    val actorId: String,
    val obligation: Obligation
)

@Serializable
data class SdpiProfileUseCaseReference(
    val profileId: String,
    val profileOptionId: String?,
    val useCaseReference: SdpiUseCaseReference
)