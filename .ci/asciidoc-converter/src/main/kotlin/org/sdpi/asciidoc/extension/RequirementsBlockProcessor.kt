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
import org.sdpi.asciidoc.model.SdpiRequirement

const val BLOCK_NAME_SDPI_REQUIREMENT = "sdpi_requirement"

private typealias RequirementNumber = Int
private typealias Occurrence = Int

/**
 * Block processor that searches for sdpi_req blocks.
 *
 * - Checks for requirement number duplicates
 * - Stores all requirements in [RequirementsBlockProcessor.detectedRequirements] for further processing
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
    ): Any = retrieveRequirement(reader, Attributes(attributes)).let { requirement ->
        logger.info { "Found SDPi requirement #${requirement.number}: $requirement" }
        requirement.asciiDocAttributes[BlockAttribute.ROLE] = REQUIREMENT_ROLE
        storeRequirement(requirement)
        createBlock(
            parent, plainContext(Contexts.SIDEBAR), mapOf(
                Options.ATTRIBUTES to attributes, // copy attributes for further processing
                ContentModel.KEY to ContentModel.COMPOUND // signify construction of a compound object
            )
        ).also {
            // make sure to separately parse contents since reader was requested by retrieveRequirement()
            // and is EOF now
            parseContent(it, requirement.asciiDocLines)
        }
    }

    private fun retrieveRequirement(reader: Reader, attributes: Attributes): SdpiRequirement {
        val matchResults = REQUIREMENT_NUMBER_FORMAT.findAll(attributes.id())
        val requirementNumber = matchResults.map { it.groupValues[1] }.toList().first().toInt()
        val maxOccurrence = attributes[BlockAttribute.MAX_OCCURRENCE]?.toInt() ?: 1

        if (intendedDuplicates.containsKey(requirementNumber)) {
            intendedDuplicates[requirementNumber] = intendedDuplicates[requirementNumber]!! + 1
        } else {
            intendedDuplicates[requirementNumber] = 1
        }

        val lines = reader.readLines()
        val requirementLevel =
            checkNotNull(resolveRequirementLevel(attributes[BlockAttribute.REQUIREMENT_LEVEL] ?: "")) {
                ("Missing requirement level for SDPi requirement #$requirementNumber").also {
                    logger.error { it }
                }
            }
        try {
            return SdpiRequirement(
                requirementNumber,
                requirementLevel,
                maxOccurrence,
                attributes,
                lines
            )
        } catch (e: Exception) {
            logger.error { "Error while processing requirement #$requirementNumber: ${e.message}" }
            throw e
        }
    }

    private fun storeRequirement(requirement: SdpiRequirement) {
        validateRequirement(requirement)
        detectedRequirements[requirement.number] = requirement
    }

    private fun validateRequirement(requirement: SdpiRequirement) {
        val reqNumberFromTitle = REQUIREMENT_TITLE_FORMAT.findAll(requirement.blockTitle)
            .map { it.groupValues[2] }.toList().first().toInt()
        check(reqNumberFromTitle == requirement.number) {
            ("SDPi requirement title format is wrong or number differs from ID: " +
                    "title=${requirement.blockTitle}, id=${requirement.blockId}").also {
                logger.error { it }
            }
        }

        checkNotNull(requirement.asciiDocAttributes[BlockAttribute.ROLE]) {
            "SDPi requirement #'${requirement.number}' has no role; expected '$REQUIREMENT_ROLE'".also {
                logger.error { it }
            }
        }

        check(!detectedRequirements.containsKey(requirement.number) ||
                intendedDuplicates[requirement.number]!! <= requirement.maxOccurrence) {
            "SDPi requirement #'${requirement.number}' already exists".also {
                logger.error { it }
            }
        }
    }
}