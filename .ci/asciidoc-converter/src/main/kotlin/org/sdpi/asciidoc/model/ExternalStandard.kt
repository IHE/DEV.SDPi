package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class ExternalStandard(
    val sourceFile: String,
    val id: String,
    val citation: String,
    val requirements: List<ExternalRequirement>,
    val icsGroups: List<String>
)

@Serializable
class ExternalRequirement (
    val requirementNumber: Int,
    val localId: String,
    val level: RequirementLevel,
    val groups: List<String>
)

/**
 * Internal DTO that matches the JSON file structure exactly.
 */
@Serializable
private data class RequirementJson(
    val requirementNumber: Int,
    val level: String,
    val localId: String,
    val icsGroup: String? = null
)

/**
 * Load an ExternalStandard from the JSON file at [path].
 *
 * @param strPath      File system path to the JSON file.
 * @param strId        Document unique id for this standard reference.
 * @param strCitation  Citation text you want to associate with this standard.
 */
fun loadExternalStandardRequirementsFromJson(
    strPath: String,
    strId: String,
    strCitation: String
): ExternalStandard {
    val file = File(strPath)
    val jsonText = file.readText()

    val json = Json {
        ignoreUnknownKeys = true // in case the JSON has extra fields
    }

    // Parse the JSON array into DTOs
    val jsonRequirements: List<RequirementJson> =
        json.decodeFromString(jsonText)

    // Map DTOs to your domain model
    val requirements = jsonRequirements.map { r ->
        ExternalRequirement(
            requirementNumber = r.requirementNumber,
            localId = r.localId,
            level = RequirementLevel.fromJson(r.level),
            groups = r.icsGroup
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
        )
    }

    // Collect distinct non-null ICS groups
    val icsGroups = requirements
        .flatMap { it.groups }
        .distinct()
        .sorted()

    return ExternalStandard(
        sourceFile = file.name,
        id = strId,
        citation = strCitation,
        requirements = requirements,
        icsGroups = icsGroups
    )
}