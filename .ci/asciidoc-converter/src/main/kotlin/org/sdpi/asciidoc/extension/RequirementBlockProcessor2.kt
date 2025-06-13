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
import java.util.*

const val BLOCK_NAME_SDPI_REQUIREMENT = "sdpi_requirement"

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
class RequirementBlockProcessor2 : BlockProcessor(BLOCK_NAME_SDPI_REQUIREMENT) {
    private companion object : Logging {
        val REQUIREMENT_TITLE_FORMAT = "^([A-Z])*?R(\\d+)$".toRegex()
        const val REQUIREMENT_ROLE = "requirement"
    }

    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any {
        val requirementNumber: Int = getRequirementNumber(attributes)
        val strGlobalId = "" //getRequirementOid(parent, requirementNumber, attributes)
        val strLinkId = String.format("r%04d", requirementNumber)

        logger.info("Found requirement #$requirementNumber at ${parent.sourceLocation}")

        attributes["id"] = strLinkId
        if (strGlobalId != null) {
            attributes["global-id"] = strGlobalId
        }
        attributes["reftext"] = String.format("R%04d", requirementNumber)
        attributes["requirement-number"] = requirementNumber
        if (strGlobalId != null) {
            attributes["title"] = formatRequirementTitle(requirementNumber, strGlobalId)
        }
        attributes["role"] = REQUIREMENT_ROLE

        // Include an empty block with an id in the global format.
        if (strGlobalId != null) {
            val anchorBlock = createBlock(parent, plainContext(Contexts.OPEN), Collections.emptyList(), mapOf())
            anchorBlock.id = strGlobalId
            parent.append(anchorBlock)
        }

        return createBlock(
            parent, plainContext(Contexts.SIDEBAR), mapOf(
                Options.ATTRIBUTES to attributes, // copy attributes for further processing
                ContentModel.KEY to ContentModel.COMPOUND, // signify construction of a compound object
            )
        )
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
            val id = parseRequirementNumber(strId.toString())
            checkNotNull(id)
            {
                "id '$strId' is not a valid requirement number".also { logger.error { it } }
            }
            return id
        }

        val strTitle = mutableAttributes["title"]
        return REQUIREMENT_TITLE_FORMAT.findAll(strTitle.toString())
            .map { it.groupValues[2] }.toList().first().toInt()
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
    ): String? {
        val strSourceSpecification = mutableAttributes[RequirementAttributes.Common.SPECIFICATION.key]

        // To simplify transition, we'll print a warning and allow the oid to be missing. .
        if (strSourceSpecification == null) {
            logger.warn("${getLocation((parent))} missing source specification for requirement #${requirementNumber}. In the future this will be an error.")
            return null
        }

        checkNotNull(strSourceSpecification) {
            ("Missing requirement source id for SDPi requirement #$requirementNumber [${getLocation(parent)}]").also {
                logger.error(it)
            }
        }
        val strSourceId = getOidFor(parent, requirementNumber, strSourceSpecification as String)

        // Global unique ids are composed of the source specification's oid,
        // ".2." + the requirement number. [[SDPi:§1:A.4.2.1]]
        return "$strSourceId.2.$requirementNumber"
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
            ("The oid id ('${strOidId}') for SDPi requirement #'${requirementNumber}' cannot be found  [${
                getLocation(
                    parent
                )
            }].").also {
                logger.error(it)
            }
        }
        return strOid as String
    }

    /**
     * Creates text for the requirement title in the document
     */
    private fun formatRequirementTitle(requirementNumber: Int, strGlobalId: String): String {
        if (strGlobalId.isEmpty()) {
            return String.format("R%04d", requirementNumber)
        } else {
            return "R${String.format("%04d", requirementNumber)} [.global-id]#`(${strGlobalId})`#"
        }
    }
}