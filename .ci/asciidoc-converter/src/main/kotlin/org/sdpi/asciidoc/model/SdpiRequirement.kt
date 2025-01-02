package org.sdpi.asciidoc.model

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.asciidoctor.ast.PhraseNode
import org.asciidoctor.ast.StructuralNode
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
 * @property description: friendly label for the requirement type (e.g., to
 * describe types in tables).
 */
enum class RequirementType(val keyword: String, val description: String)
{
    // A requirement in a high-level, profile-independent use case specification
    USE_CASE("use_case_feature", "Use case feature" ),

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

enum class RiskMitigationType(val keyword: String, val description : String)
{
    GENERAL("general", "General risk mitigation"),
    SAFETY("safety", "Safety risk mitigation"),
    EFFECTIVENESS("effectiveness", "Effectiveness risk mitigation"),
    SECURITY("security", "Security risk mitigation"),
    AUDIT("audit", "Traceability risk mitigation"),
}

enum class RiskMitigationTestability(val keyword: String, val description : String)
{
    INSPECTION("inspect", "Verification requires behaviour inspection"),
    INTEROPERABILITY("wire", "Verification can be done by a test tool")
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

    fun makeLink() : String
    {
        return "link:#$blockId[$localId]"
    }
}

@Serializable
data class RequirementSpecification(
    val normativeContent : List<Content>,
    val noteContent : List<Content>,
    val exampleContent : List<Content>,
    val relatedContent : List<Content>)
{

}

@Serializable
sealed class SdpiRequirement2
{
    abstract val requirementNumber : Int
    abstract val localId : String
    abstract val globalId : String
    abstract val level : RequirementLevel
    abstract val groups : List<String>
    abstract val specification: RequirementSpecification

    /**
     * Human friendly description of the requirement type.
     */
    abstract fun getTypeDescription() : String

    fun makeLink(): String
    {
        return "link:#${getBlockId()}[${localId}]"
    }

    fun getBlockId(): String
    {
        return globalId
    }

    @Serializable
    @SerialName("tech-feature")
    data class TechFeature(
        override val requirementNumber : Int,
        override val localId : String,
        override val globalId : String,
        override val level : RequirementLevel,
        override val groups : List<String>,
        override val specification: RequirementSpecification) : SdpiRequirement2()
    {
        override fun getTypeDescription(): String
        {
            return RequirementType.TECH.description
        }
    }


    @Serializable
    @SerialName("use-case")
    data class UseCase(
        override val requirementNumber : Int,
        override val localId : String,
        override val globalId : String,
        override val level : RequirementLevel,
        override val groups : List<String>,
        override val specification: RequirementSpecification,
        val useCaseId : String) : SdpiRequirement2()
    {
        override fun getTypeDescription(): String
        {
            return RequirementType.USE_CASE.description
        }
    }


    @Serializable
    @SerialName("ref-ics")
    data class ReferencedImplementationConformanceStatement(
        override val requirementNumber : Int,
        override val localId : String,
        override val globalId : String,
        override val level : RequirementLevel,
        override val groups : List<String>,
        override val specification: RequirementSpecification,
        val referenceId : String,
        val referenceSource : String,
        val referenceSection : String,
        val referenceRequirement : String) : SdpiRequirement2()
    {
        override fun getTypeDescription(): String
        {
            return RequirementType.REF_ICS.description
        }
    }

    @Serializable
    @SerialName("risk-mitigation")
    data class RiskMitigation(
        override val requirementNumber : Int,
        override val localId : String,
        override val globalId : String,
        override val level : RequirementLevel,
        override val groups : List<String>,
        override val specification: RequirementSpecification,
        val mitigating : RiskMitigationType,
        val test : RiskMitigationTestability) : SdpiRequirement2()
    {
        override fun getTypeDescription(): String
        {
            return RequirementType.RISK_MITIGATION.description
        }
    }

}


/**
 * Storage for content elements (e.g., requirement normative statements,
 * notes, etc). Supports writing to json format.
 */
@Serializable
sealed class Content()
{
    abstract fun countKeyword(strKeyword : String) : Int

    @Serializable
    @SerialName("block")
    data class Content_Block(val lines : List<String>) : Content()
    {
        override fun countKeyword(strKeyword : String) : Int
        {
            val searcher = Regex(strKeyword)
            var nCount : Int = 0
            for(str in lines)
            {
                nCount += searcher.findAll(str).count()
            }

            return nCount
        }
    }

    @Serializable
    @SerialName("listing")
    data class Content_Listing(val title : String, val lines : List<String>) : Content()
    {
        override fun countKeyword(strKeyword : String) : Int
        {
            val searcher = Regex(strKeyword)
            var nCount : Int = searcher.findAll(title).count()
            for(str in lines)
            {
                nCount += searcher.findAll(str).count()
            }

            return nCount
        }
    }

    @Serializable
    @SerialName("olist")
    data class Content_OrderedList(val items : List<Content>): Content()
    {
        override fun countKeyword(strKeyword: String): Int
        {
            var nCount : Int = 0
            for(item in items)
            {
                nCount += item.countKeyword(strKeyword)
            }
            return nCount
        }
    }

    @Serializable
    @SerialName("ulist")
    data class Content_UnorderedList(val items : List<Content>): Content()
    {
        override fun countKeyword(strKeyword: String): Int
        {
            var nCount : Int = 0
            for(item in items)
            {
                nCount += item.countKeyword(strKeyword)
            }
            return nCount
        }
    }

    @Serializable
    @SerialName("item")
    data class Content_ListItem(val strMarker : String, val strText : String): Content()
    {
        override fun countKeyword(strKeyword : String) : Int
        {
            val searcher = Regex(strKeyword)
            return  searcher.findAll(strText).count()
        }
    }
}

fun countKeyword(strKeyword: String, contents : List<Content>) : Int
{
    var nCount : Int = 0
    for (content in contents)
    {
        nCount += content.countKeyword(strKeyword)
    }
    return nCount
}
