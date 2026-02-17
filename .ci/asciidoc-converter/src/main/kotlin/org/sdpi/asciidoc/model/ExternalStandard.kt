package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class ExternalStandard(
    val sourceFile: String,
    val id: String,
    val citationKey: String,
    val requirements: List<ExternalRequirement>,
    val icsGroups: List<String>
) {
    fun getRequirement(strRequirementId: String) : ExternalRequirement? {
        return requirements.firstOrNull{ strRequirementId == it.localId }
    }
}

@Serializable
class ExternalRequirement (
    val requirementNumber: Int,
    val localId: String,
    val level: RequirementLevel,
    val groups: List<String>
) {
    private val sdpiSupport: MutableList<ExternalRequirementSupport> = mutableListOf()

    fun addSupport(support: ExternalRequirementSupport) {
        sdpiSupport.add(support)
    }

    fun localSupport(): List<ExternalRequirementSupport> = sdpiSupport
}

@Serializable
class ExternalRequirementSupport(
    val support: SdpiRequirement2,
    val comment: String?)

class ExternalRequirementReference(
    val standard: ExternalStandard,
    val requirement: ExternalRequirement,
)
{
    companion object {
        val REQUIREMENT_REF_REGEX = """RefRequirement:([a-zA-Z0-9_-]+)\[(.+)]""".toRegex()
        val REQUIREMENT_REF_ARGS = ",(?=(?:[^\"']|\"[^\"]*\"|'[^']*')*$)".toRegex()
        val REQUIREMENT_ARG_PAIRS = """([A-Za-z0-9_-]+)=(.*)""".toRegex()
    }
}

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
 * @param strCitation  Citation key you want to associate with this standard.
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
        citationKey = strCitation,
        requirements = requirements,
        icsGroups = icsGroups
    )
}