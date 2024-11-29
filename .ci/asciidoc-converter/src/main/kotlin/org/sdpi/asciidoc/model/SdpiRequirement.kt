package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable
import org.sdpi.asciidoc.*

/**
 * Definition of requirement levels.
 *
 * @property keyword The keyword as it is supposed to appear as a value to the attribute key.
 */
enum class RequirementLevel(val keyword: String) {
    MAY("may"),
    SHOULD("should"),
    SHALL("shall")
}

/**
 * Define requirement types. See SDPi Requirements Core Model [[SDPi:§1:A.4.1]]
 * @property keyword: the ascii doc attribute key.
 */
enum class RequirementType(val keyword: String)
{
    // A requirement in a high-level, profile-independent use case specification
    USE_CASE("use_case_feature"),

    // A basic technical requirement that is not one of the other categories
    // and does not require additional metadata.
    TECH("tech_feature"),

    // Requirement associated to an aspect of an IHE profile specification
    IHE_PROFILE("ihe_profile"),

    // A requirement linked to an implementation conformance statement in
    // a referenced standard
    REF_ICS("ref_ics"),

    // Requirement that represents a quality aspect of the specification typically
    // related to risk management activities and resulting mitigations
    RISK_MITIGATION("risk_mitigation"),
}

/**
 * Structure that represents an SDPi requirement.
 *
 * @property number The requirement number as an integer (no leading R or zero-padding).
 * @property globalId The requirement id as a globally unique oid.
 * @property type The type of requirement.
 * @property level The requirement level as specified by the attribute [BlockAttribute.REQUIREMENT_LEVEL].
 * @property maxOccurrence The number of occurrences in the document (some requirements are copy-pasted for sake of readability)
 * @property asciiDocAttributes All attributes captured by the block that represents this requirement.
 * @property asciiDocLines The actual ASCIIdoc source.
 * @property blockId Identifier for the block, e.g., for linking
 * @property localId The local requirement id (e.g., R1010)
 * @property blockTitle The title used for the block defining the requirement.
 */
@Serializable
data class SdpiRequirement(
    val number: Int,
    val globalId: String,
    val type : RequirementType,
    val level: RequirementLevel,
    val groups : List<String>,
    val maxOccurrence: Int,
    val asciiDocAttributes: Attributes,
    val asciiDocLines: List<String>
) {
    val blockId = globalId
    val localId = String.format("R%04d", number)
    val blockTitle = asciiDocAttributes.title()
}