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
 * Structure that represents an SDPi requirement.
 *
 * @property number The requirement number as an integer (no leading R or zero-padding).
 * @property level The requirement level as specified by the attribute [BlockAttribute.REQUIREMENT_LEVEL].
 * @property maxOccurrence The number of occurrences in the document (some requirements are copy-pasted for sake of readability)
 * @property asciiDocAttributes All attributes captured by the block that represents this requirement.
 * @property asciiDocLines The actual ASCIIdoc source.
 */
@Serializable
data class SdpiRequirement(
    val number: Int,
    val level: RequirementLevel,
    val maxOccurrence: Int,
    val asciiDocAttributes: Attributes,
    val asciiDocLines: List<String>
) {
    val blockId = asciiDocAttributes.id()
    val blockTitle = asciiDocAttributes.title()
}