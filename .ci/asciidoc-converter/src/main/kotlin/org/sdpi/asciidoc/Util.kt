package org.sdpi.asciidoc

import org.apache.logging.log4j.kotlin.loggerOf
import org.asciidoctor.ast.StructuralNode
import org.sdpi.asciidoc.model.RequirementLevel
import org.sdpi.asciidoc.model.StructuralNodeWrapper
import org.sdpi.asciidoc.model.toSealed

/**
 * Resolves the block id attribute from an attributes map or throws otherwise.
 */
fun Attributes.id() = this[BlockAttribute.ID] ?: throw Exception("Block identifier is missing")

/**
 * Resolves the block title attribute from an attributes map or throws otherwise.
 */
fun Attributes.title() = this[BlockAttribute.TITLE] ?: throw Exception("Block title is missing")

/**
 * Creates a context name without a leading colon.
 *
 * `BlockProcessor.createBlock()` requires a context name without a leading colon.
 * Use this function to check the format and remove the leading colon for you.
 */
fun plainContext(context: String) = "^:([a-z]+)$".toRegex()
    .findAll(context)
    .map { it.groupValues[1] }
    .toList()
    .first()

/**
 * Checks if an expression holds true, prints out an error message and throws if not.
 *
 * @param value The expression that is tested.
 * @param node The node from which file and line number is extracted. Make sure map source is enabled.
 * @param msg A function that creates the error message.
 */
fun validate(value: Boolean, node: StructuralNode, msg: () -> String) {
    if (value) {
        return
    }

    val msgWithLocation = "Error in file ${node.sourceLocation.path}@${node.sourceLocation.lineNumber}: ${msg()}"
    checkNotNull(node.sourceLocation) { "Fatal error: map source disabled" }
    loggerOf(Any::class.java).error { msgWithLocation }
    throw Exception(msgWithLocation)
}

/**
 * Checks if a node is an appendix block.
 *
 * @return true if yes, false otherwise.
 */
fun StructuralNode.isAppendix() = when (val section = this.toSealed()) {
    is StructuralNodeWrapper.Section -> section.wrapped.sectionName == "appendix"
    else -> false
}

fun parseRequirementNumber(strRequirement: String): Int? {
    val requirementParser = "^r(\\d+)$".toRegex()
    val matchResults = requirementParser.findAll(strRequirement)
    if (matchResults.any()) {
        return matchResults.map { it.groupValues[1] }.toList().first().toInt()
    }

    return null
}

/**
 * Takes a string and converts it to a [RequirementLevel] enum.
 *
 * @param raw Raw text being shall, should or may.
 *
 * @return the [RequirementLevel] enum or null if the conversion failed (raw was not shall, should or may).
 */
fun resolveRequirementLevel(raw: String) = RequirementLevel.entries.firstOrNull { it.keyword == raw }

/**
 * Converts an object, typically from an attribute map, into
 * a list of groups.
 */
fun getRequirementGroups(oGroups: Any?): List<String> {
    if (oGroups == null) {
        return listOf()
    }
    return oGroups.toString().split(",")
}

fun getLocation(block: StructuralNode): String {
    return "${block.sourceLocation.path}:${block.sourceLocation.lineNumber}"
}
