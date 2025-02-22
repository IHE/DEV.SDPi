package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.Options
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import org.sdpi.asciidoc.*
import org.sdpi.asciidoc.model.RequirementLevel
import org.sdpi.asciidoc.model.RequirementType
import org.sdpi.asciidoc.model.SdpiRequirement

const val BLOCK_NAME_SDPI_REQUIREMENT = "sdpi_requirement"

private typealias RequirementNumber = Int
private typealias Occurrence = Int

/**
 * Block processor for sdpi_requirement blocks.
 * Requirement blocks provide metadata for profile requirements. Metadata is
 * provided through block attributes and includes:
 *    - level (sdpi_req_level): may|should|shall
 *    - type (sdpi_req_type): one of
 *          - tech_feature: a basic technical requirement that isn't one of the other categories
 *          - use_case_feature: a requirement for a profile-independent use case specification
 *          - ihe_profile: requirement associated to an aspect of an IHE profile specification
 *          - ref_ics: referenced implementation conformance statement from another standard
 *          - risk_mitigation: requirement typically related to risk management
 *    - groups (sdpi_req_group): comma separated list of groups that the requirement belongs to;
 *          may be used as a filter by the RequirementListMacroProcessor
 *    - sdpi_req_specification: id of specification that owns the requirement; specifications are
 *          defined in document level attribute entries of the form "spid_oid.<owner>" and
 *          map to an oid for the specification.
 *
 *  The requirement number is determine from the block id or title.
 *
 * - Checks for requirement number duplicates
 * - Stores all requirements in [RequirementsBlockProcessor.detectedRequirements] for further processing
 *
 * Example:
 * // Define object ids for referenced standards
 * :sdpi_oid.sdpi: 1.3.6.1.4.1.19376.1.6.2.10.1.1.1
 * :sdpi_oid.sdpi-p: 1.3.6.1.4.1.19376.1.6.2.11
 * :sdpi_oid.sdpi-a: 1.3.6.1.4.1.19376.1.6.2.x
 *
 * // An example requirement block
 * .R1021
 * [sdpi_requirement,sdpi_req_level=shall,sdpi_req_type=tech_feature,sdpi_req_group="provider,discovery-proxy",sdpi_req_specification=sdpi-p]
 * ****
 *
 * [NORMATIVE]
 * When the vol1_spec_sdpi_p_option_discovery_proxy is enabled for a vol1_spec_sdpi_p_actor_somds_provider Actor, then it shall use the vol2_clause_dev_46, DEV-46 transaction to update the vol1_spec_sdpi_p_actor_discovery_proxy Actor on its network presence and departure.
 *
 * [NOTE]
 * ====
 * . doh
 * . ray
 * . me
 * ====
 *
 * .Related
 * [%collapsible]
 * ====
 * . <<ref_w3c_ws_eventing_2006>>, section 3.1
 * . <<ref_ieee_11073_10207_2017>>, §C.57, R0121
 * ====
 *
 * ****
 */
@Name(BLOCK_NAME_SDPI_REQUIREMENT)
@Contexts(Contexts.SIDEBAR)
@ContentModel(ContentModel.COMPOUND)
class RequirementsBlockProcessor : BlockProcessor(BLOCK_NAME_SDPI_REQUIREMENT) {
    private val intendedDuplicates = mutableMapOf<RequirementNumber, Occurrence>()

    private companion object : Logging {
        val REQUIREMENT_NUMBER_FORMAT = "^r(\\d+)$".toRegex()
        val REQUIREMENT_TITLE_FORMAT = "^([A-Z])*?R(\\d+)$".toRegex()
        const val REQUIREMENT_ROLE = "requirement"
    }

    private val detectedRequirements = mutableMapOf<Int, SdpiRequirement>()

    /**
     * Returns all requirements that were collected by this block processor.
     *
     * Make sure to only call this function once the conversion ended as otherwise this map will be empty.
     */
    fun detectedRequirements(): Map<Int, SdpiRequirement> = detectedRequirements

    override fun process(
        parent: StructuralNode, reader: Reader,
        attributes: MutableMap<String, Any>
    ): Any = retrieveRequirement(
        parent,
        reader,
        attributes
    ).let { requirement ->
        logger.info { "Found SDPi requirement #${requirement.number}" }//: $requirement" }
        requirement.asciiDocAttributes[BlockAttribute.ROLE] = REQUIREMENT_ROLE
        attributes["role"] = REQUIREMENT_ROLE
        storeRequirement(requirement)
        createBlock(
            parent, plainContext(Contexts.SIDEBAR), mapOf(
                Options.ATTRIBUTES to attributes, // copy attributes for further processing
                ContentModel.KEY to ContentModel.COMPOUND, // signify construction of a compound object

            )
        ).also {
            // make sure to separately parse contents since reader was requested by retrieveRequirement()
            // and is EOF now
            parseContent(it, requirement.asciiDocLines)
        }
    }

    private fun retrieveRequirement(
        parent: StructuralNode,
        reader: Reader,
        mutableAttributes: MutableMap<String, Any>
    ): SdpiRequirement {
        val requirementNumber: Int = getRequirementNumber(mutableAttributes)
        val strGlobalId = getRequirementOid(parent, requirementNumber, mutableAttributes)
        val aGroups: List<String> = getRequirementGroupMembership(mutableAttributes)
        val requirementLevel: RequirementLevel = getRequirementLevel(requirementNumber, mutableAttributes)
        val requirementType: RequirementType = getRequirementType(requirementNumber, mutableAttributes)

        mutableAttributes["id"] = strGlobalId
        mutableAttributes["requirement-number"] = requirementNumber
        mutableAttributes["title"] = formatRequirementTitle(requirementNumber, strGlobalId)

        val attributes = Attributes.create(mutableAttributes)
        val maxOccurrence = attributes[BlockAttribute.MAX_OCCURRENCE]?.toInt() ?: 1

        if (intendedDuplicates.containsKey(requirementNumber)) {
            intendedDuplicates[requirementNumber] = intendedDuplicates[requirementNumber]!! + 1
        } else {
            intendedDuplicates[requirementNumber] = 1
        }

        val lines = reader.readLines()
        //val lines = listOf<String>()

        try {
            return SdpiRequirement(
                requirementNumber,
                strGlobalId,
                requirementType,
                requirementLevel,
                aGroups,
                maxOccurrence,
                attributes,
                lines
            )
        } catch (e: Exception) {
            logger.error { "Error while processing requirement #$requirementNumber: ${e.message}" }
            throw e
        }
    }

    /**
     * Retrieve the requirement level
     */
    private fun getRequirementLevel(requirementNumber: Int, attributes: MutableMap<String, Any>): RequirementLevel {
        val strLevel = attributes[BlockAttribute.REQUIREMENT_LEVEL.key]
        checkNotNull(strLevel) {
            ("Missing ${BlockAttribute.REQUIREMENT_LEVEL.key} attribute for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }
        val reqLevel = resolveRequirementLevel(strLevel.toString())
        checkNotNull(reqLevel) {
            ("Invalid requirement level '${strLevel}' for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }

        return reqLevel
    }

    /**
     * Retrieve the requirement type
     */
    private fun getRequirementType(requirementNumber: Int, attributes: MutableMap<String, Any>): RequirementType {
        val strType = attributes[BlockAttribute.REQUIREMENT_TYPE.key]
        checkNotNull(strType) {
            ("Missing ${BlockAttribute.REQUIREMENT_TYPE.key} attribute for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }
        val reqType = RequirementType.entries.firstOrNull { it.keyword == strType }
        checkNotNull(reqType) {
            ("Invalid requirement type '${strType}' for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }

        return reqType
    }

    /**
     * Retrieve the requirement number from available attributes.
     * The requirement number may be specified as a block id or title.
     * Requirement numbers must match the format defined by REQUIREMENT_NUMBER_FORMAT
     * or REQUIREMENT_TITLE_FORMAT for the id or title, respectively.
     */
    private fun getRequirementNumber(mutableAttributes: MutableMap<String, Any>): Int {
        val strId = mutableAttributes["id"]
        if (strId != null) {
            val matchResults = REQUIREMENT_NUMBER_FORMAT.findAll(strId.toString())
            return matchResults.map { it.groupValues[1] }.toList().first().toInt()
        }

        val strTitle = mutableAttributes["title"]
        return REQUIREMENT_TITLE_FORMAT.findAll(strTitle.toString())
            .map { it.groupValues[2] }.toList().first().toInt()
    }

    /**
     * Retrieves the list of groups the requirement belongs to (if any).
     */
    private fun getRequirementGroupMembership(mutableAttributes: MutableMap<String, Any>): List<String> {
        return getRequirementGroups(mutableAttributes[BlockAttribute.REQUIREMENT_GROUPS.key])
    }

    /**
     * Creates text for the requirement title in the document
     */
    private fun formatRequirementTitle(requirementNumber: Int, strGlobalId: String): String {
        return "R${String.format("%04d", requirementNumber)} [.global-id]#`(${strGlobalId})`#"
    }

    private fun storeRequirement(requirement: SdpiRequirement) {
        validateRequirement(requirement)
        detectedRequirements[requirement.number] = requirement
    }

    private fun validateRequirement(requirement: SdpiRequirement) {

        checkNotNull(requirement.asciiDocAttributes[BlockAttribute.ROLE]) {
            "SDPi requirement #'${requirement.number}' has no role; expected '$REQUIREMENT_ROLE'".also {
                logger.error { it }
            }
        }

        check(
            !detectedRequirements.containsKey(requirement.number) ||
                    intendedDuplicates[requirement.number]!! <= requirement.maxOccurrence
        ) {
            "SDPi requirement #'${requirement.number}' already exists".also {
                logger.error { it }
            }
        }
    }

    /**
     * Retrieves the oid definition corresponding to an oid id.
     * Oids are defined as document level attributes in the spid_oid configuration scope. For
     * example:
     * :sdpi_oid.sdpi: 1.3.6.1.4.1.19376.1.6.2.10.1.1.1
     */
    private fun getOidFor(parent: StructuralNode, requirementNumber: Int, strOidId: String): String {
        val document = parent.document
        val strAttribute = "sdpi_oid${strOidId}"
        val strOid = document.attributes[strAttribute]
        checkNotNull(strOid) {
            ("The oid id ('${strOidId}') for SDPi requirement #'${requirementNumber}' cannot be found.").also {
                logger.error(it)
            }
        }
        return strOid as String
    }

    /**
     * Retrieve the globally unique object id for the requirement.
     * See Assigning Unique Identifiers [[SDPi:§1:A.4.2.1]]
     *
     */
    private fun getRequirementOid(
        parent: StructuralNode,
        requirementNumber: Int,
        mutableAttributes: MutableMap<String, Any>
    ): String {
        val strSourceSpecification = mutableAttributes[BlockAttribute.REQUIREMENT_SPECIFICATION.key]
        checkNotNull(strSourceSpecification) {
            ("Missing requirement source id for SDPi requirement #$requirementNumber").also {
                logger.error(it)
            }
        }
        val strSourceId = getOidFor(parent, requirementNumber, strSourceSpecification as String)

        // Global unique ids are composed of the source specification's oid,
        // ".2." + the requirement number. [[SDPi:§1:A.4.2.1]]
        return "$strSourceId.2.$requirementNumber"
    }
}

