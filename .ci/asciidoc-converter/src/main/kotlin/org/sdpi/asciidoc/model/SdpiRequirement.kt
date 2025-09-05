package org.sdpi.asciidoc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sdpi.asciidoc.extension.Roles
import org.sdpi.asciidoc.makeLink

/**
 * Definition of requirement levels.
 *
 * @property keyword The keyword as it is supposed to appear as a value to the attribute key.
 */
enum class RequirementLevel(val keyword: String, val icsStatus: String) {
    MAY("may", "p"),
    SHOULD("should", "r"),
    SHALL("shall", "m")
}

enum class OwningContext(val roleKeyword: String, val label: String) {
    @SerialName("profile")
    PROFILE(Roles.Profile.PROFILE.key, "Sdpi Profile"),

    @SerialName("profile-option")
    PROFILE_OPTION(Roles.Profile.PROFILE_OPTION.key, "Sdpi Profile Option"),

    @SerialName("content-module")
    CONTENT_MODULE(Roles.ContentModule.SECTION_ROLE.key, "Content module"),

    @SerialName("gateway")
    GATEWAY(Roles.Gateway.SECTION_ROLE.key, "Gateway"),

    @SerialName("protocol")
    PROTOCOL(Roles.Protocol.SECTION_ROLE.key, "Protocol"),

    @SerialName("use-case")
    USE_CASE(Roles.UseCase.FEATURE.key, "Use case"),
}

/**
 * Takes a string and converts it to a [OwningContext] enum.
 *
 * @param strRole Raw text being shall, should or may.
 *
 * @return the [OwningContext] enum or null if the conversion failed (raw was not shall, should or may).
 */
fun parseOwnerFromRole(strRole: String) = OwningContext.entries.firstOrNull { it.roleKeyword == strRole }


/**
 * Define requirement types. See SDPi Requirements Core Model [[SDPi:§1:A.4.1]]
 * @property keyword: the ascii doc attribute key.
 * @property description: friendly label for the requirement type (e.g., to
 * describe types in tables).
 */
enum class RequirementType(val keyword: String, val description: String) {
    // A requirement in a high-level, profile-independent use case specification
    USE_CASE("use_case_feature", "Use case feature"),

    // A basic technical requirement that is not one of the other categories
    // and does not require additional metadata.
    TECH("tech_feature", "Technical feature"),

    // Requirement associated to an aspect of an IHE profile specification
    IHE_PROFILE("ihe_profile", "IHE profile"),

    // A requirement linked to an implementation conformance statement in
    // a referenced standard
    REF_ICS("ref_ics", "Referenced implementation conformance statement"),

    // Requirement that represents a quality aspect of the specification typically
    // related to risk management activities and resulting mitigations
    RISK_MITIGATION("risk_mitigation", "Risk management and mitigation"),
}

enum class RiskMitigationType(val keyword: String, val description: String) {
    GENERAL("general", "General risk mitigation"),
    SAFETY("safety", "Safety risk mitigation"),
    EFFECTIVENESS("effectiveness", "Effectiveness risk mitigation"),
    SECURITY("security", "Security risk mitigation"),
    AUDIT("audit", "Traceability risk mitigation"),
}

enum class RiskMitigationTestability(val keyword: String, val description: String) {
    INSPECTION("inspect", "Verification requires behaviour inspection"),
    INTEROPERABILITY("wire", "Verification can be done by a test tool")
}

@Serializable
data class RequirementSpecification(
    val normativeContent: List<Content>,
    val noteContent: List<Content>,
    val exampleContent: List<Content>,
    val relatedContent: List<Content>
) {
    fun getActorIds(): List<String> {
        val ids = mutableListOf<String>()
        for (content in normativeContent) {
            ids.addAll(content.getIdMatches(SdpiActor.ACTOR_ID_REGEX, 1))
            ids.addAll(content.getIdMatches(SdpiActor.ACTOR_REF_REGEX, 1))
        }
        return ids.distinct()
    }
}

@Serializable
data class RequirementContext(
    val type: OwningContext,
    val id: String,
)

/**
 * Structure that represents an SDPi requirement.
 *
 * @property requirementNumber The requirement number as an integer (no leading R or zero-padding).
 * @property globalId The requirement id as a globally unique oid, or empty string if we don't have one yet.
 * @property level The requirement level as specified by the attribute [RequirementAttributes.Common.LEVEL].
 * @property localId The local requirement id (e.g., R1010)
 * @property groups Arbitrary groups that the requirement belongs to
 * @property specification Formal specification for the requirement
 */
@Serializable
sealed class SdpiRequirement2 {
    abstract val requirementNumber: Int
    abstract val localId: String
    abstract val oid: String
    abstract val level: RequirementLevel
    abstract val owner: RequirementContext?
    abstract val groups: List<String>
    abstract val specification: RequirementSpecification

    /**
     * Human friendly description of the requirement type.
     */
    abstract fun getTypeDescription(): String

    fun makeLinkLocal(): String {
        return makeLink(getBlockId(), localId)
    }

    fun makeLinkGlobal(): String {
        if (oid.isNotEmpty()) {
            return makeLink(getBlockId(), oid)
        }
        return makeLinkLocal()
    }

    fun getBlockId(): String {
        if (oid.isNotEmpty()) {
            return oid
        }

        return String.format("r%04d", requirementNumber)
    }

    @Serializable
    @SerialName("actors")
    private val actorsReferenced = mutableListOf<String>()

    fun actors(): List<String> = actorsReferenced

    fun gatherActors(actorAliases: Map<String, String>) {
        val actors = specification.getActorIds().distinct().map { actorAliases[it] ?: it }
        actorsReferenced.addAll(actors)
    }

    @Serializable
    @SerialName("tech-feature")
    data class TechFeature(
        override val requirementNumber: Int,
        override val localId: String,
        override val oid: String,
        override val level: RequirementLevel,
        override val owner: RequirementContext?,
        override val groups: List<String>,
        override val specification: RequirementSpecification
    ) : SdpiRequirement2() {
        override fun getTypeDescription(): String {
            return RequirementType.TECH.description
        }
    }


    @Serializable
    @SerialName("use-case")
    data class UseCase(
        override val requirementNumber: Int,
        override val localId: String,
        override val oid: String,
        override val level: RequirementLevel,
        override val owner: RequirementContext?,
        override val groups: List<String>,
        override val specification: RequirementSpecification,
        val useCaseId: String
    ) : SdpiRequirement2() {
        override fun getTypeDescription(): String {
            return RequirementType.USE_CASE.description
        }
    }


    @Serializable
    @SerialName("ref-ics")
    data class ReferencedImplementationConformanceStatement(
        override val requirementNumber: Int,
        override val localId: String,
        override val oid: String,
        override val level: RequirementLevel,
        override val owner: RequirementContext?,
        override val groups: List<String>,
        override val specification: RequirementSpecification,
        val referenceId: String,
        val referenceSource: String,
        val referenceSection: String,
        val referenceRequirement: String
    ) : SdpiRequirement2() {
        override fun getTypeDescription(): String {
            return RequirementType.REF_ICS.description
        }
    }

    @Serializable
    @SerialName("risk-mitigation")
    data class RiskMitigation(
        override val requirementNumber: Int,
        override val localId: String,
        override val oid: String,
        override val level: RequirementLevel,
        override val owner: RequirementContext?,
        override val groups: List<String>,
        override val specification: RequirementSpecification,
        val mitigating: RiskMitigationType,
        val test: RiskMitigationTestability
    ) : SdpiRequirement2() {
        override fun getTypeDescription(): String {
            return RequirementType.RISK_MITIGATION.description
        }
    }

}


/**
 * Storage for content elements (e.g., requirement normative statements,
 * notes, etc). Supports writing to json format.
 */
@Serializable
sealed class Content {
    abstract fun countKeyword(strKeyword: String): Int

    abstract fun getIdMatches(reActor: Regex, nMatchIndex: Int): List<String>

    @Serializable
    @SerialName("block")
    data class Block(val lines: List<String>) : Content() {
        override fun countKeyword(strKeyword: String): Int {
            val searcher = Regex(strKeyword)
            var nCount = 0
            for (str in lines) {
                nCount += searcher.findAll(str).count()
            }

            return nCount
        }

        override fun getIdMatches(reActor: Regex, nMatchIndex: Int): List<String> {
            val actorIds = mutableListOf<String>()
            for (strLine in lines) {
                actorIds.addAll(reActor.findAll(strLine).map { it -> it.groupValues[nMatchIndex] })
            }
            return actorIds
        }
    }

    @Serializable
    @SerialName("listing")
    data class Listing(val title: String, val lines: List<String>) : Content() {
        override fun countKeyword(strKeyword: String): Int {
            val searcher = Regex(strKeyword)
            var nCount: Int = searcher.findAll(title).count()
            for (str in lines) {
                nCount += searcher.findAll(str).count()
            }

            return nCount
        }

        override fun getIdMatches(reActor: Regex, nMatchIndex: Int): List<String> {
            return emptyList()
        }
    }

    @Serializable
    @SerialName("olist")
    data class OrderedList(val items: List<Content>) : Content() {
        override fun countKeyword(strKeyword: String): Int {
            var nCount = 0
            for (item in items) {
                nCount += item.countKeyword(strKeyword)
            }
            return nCount
        }

        override fun getIdMatches(reActor: Regex, nMatchIndex: Int): List<String> {
            val actorIds = mutableListOf<String>()
            for (item in items) {
                actorIds.addAll(item.getIdMatches(reActor, nMatchIndex))
            }
            return actorIds
        }
    }

    @Serializable
    @SerialName("ulist")
    data class UnorderedList(val items: List<Content>) : Content() {
        override fun countKeyword(strKeyword: String): Int {
            var nCount = 0
            for (item in items) {
                nCount += item.countKeyword(strKeyword)
            }
            return nCount
        }

        override fun getIdMatches(reActor: Regex, nMatchIndex: Int): List<String> {
            val actorIds = mutableListOf<String>()
            for (item in items) {
                actorIds.addAll(item.getIdMatches(reActor, nMatchIndex))
            }
            return actorIds
        }
    }

    @Serializable
    @SerialName("item")
    data class ListItem(val strMarker: String, val strText: String) : Content() {
        override fun countKeyword(strKeyword: String): Int {
            val searcher = Regex(strKeyword)
            return searcher.findAll(strText).count()
        }

        override fun getIdMatches(reActor: Regex, nMatchIndex: Int): List<String> {
            return reActor.findAll(strText).map { it -> it.groupValues[nMatchIndex] }.toList()
        }
    }
}

fun countKeyword(strKeyword: String, contents: List<Content>): Int {
    var nCount = 0
    for (content in contents) {
        nCount += content.countKeyword(strKeyword)
    }
    return nCount
}
