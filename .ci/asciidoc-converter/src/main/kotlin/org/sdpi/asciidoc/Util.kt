package org.sdpi.asciidoc

import org.apache.logging.log4j.kotlin.loggerOf
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.StructuralNode
import org.jruby.util.ResourceException.InvalidArguments
import org.sdpi.asciidoc.extension.Roles
import org.sdpi.asciidoc.model.BlockOwner
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
    val requirementParser = "^r(\\d+)$".toRegex(RegexOption.IGNORE_CASE)
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

fun getRequirementActors(oActors: Any?): List<String> {
    if (oActors == null) {
        return listOf()
    }

    return oActors.toString().split(",")
}

fun findIdFromParent(parent: ContentNode, strRole: String, strIdAttribute: String): String? {
    return if (parent.hasRole(strRole)) {
        parent.attributes[strIdAttribute]?.toString()
    } else if (parent.parent != parent.document) {
        findIdFromParent(parent.parent, strRole, strIdAttribute)
    } else {
        null
    }
}

fun findProfileId(parent: ContentNode): Pair<String?, String?> {
    if (parent.hasRole(Roles.Profile.PROFILE.key)) {
        val strProfileId = parent.attributes[Roles.Profile.ID.key]?.toString()
        checkNotNull(strProfileId) {
            throw IllegalStateException("Profile has no id")
        }
        return Pair(strProfileId, null)
    } else if (parent.hasRole(Roles.Profile.PROFILE_OPTION.key)) {
        val strOptionId = parent.attributes[Roles.Profile.ID_PROFILE_OPTION.key]?.toString()
        val parentId = findProfileId(parent.parent)
        return Pair(parentId.first, strOptionId)
    } else if (parent.parent != parent.document) {
        return findProfileId(parent.parent)
    } else {
        return Pair(null, null)
    }
}

fun gatherOwners(parent: StructuralNode): BlockOwner? {
    if (parent != parent.document) {
        return BlockOwner(parent.title, parent.role, gatherOwners(parent.parent as StructuralNode))
    } else {
        return null
    }
}

fun getLocation(block: StructuralNode): String {
    return if (block.sourceLocation != null) {
        "${block.sourceLocation.path}:${block.sourceLocation.lineNumber}"
    } else {
        "";
    }
}

fun getTitleFrom(block: StructuralNode): String {
    if (block.reftext != null) {
        return block.reftext
    }

    val strLabel = block.title
    val reExtractTitleElements = Regex("""^\d+([.:]\d+)*\s+(.*)""")
    val mrTitleElements = reExtractTitleElements.find(strLabel)
    return mrTitleElements?.groups?.get(2)?.value ?: strLabel
}

fun makeLink(strAnchor: String, strText: String, strClass: String? = null): String {
    if (strAnchor.contains(" ")) {
        throw  InvalidArguments("Anchor '$strAnchor' contains spaces")
    }
    if (strClass == null) {
        return "link:#$strAnchor[$strText]"
    } else {
        return "link:#$strAnchor[$strText,role=${strClass}]"
    }
}