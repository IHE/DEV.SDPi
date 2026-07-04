package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.DeprecationAttributes
import org.sdpi.asciidoc.model.DeprecatedOid
import org.sdpi.asciidoc.model.makeConformityVersionOid

const val BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT = "DeprecateRequirement"

@Name(BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT)
class DeprecateRequirementProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT) {
    private companion object : Logging

    private val entries = mutableMapOf<String, DeprecatedOid>()

    fun requirements() : Map<String, DeprecatedOid> {
        return entries
    }

    fun isDeprecated(strRequirementOid: String) : DeprecatedOid? {
        return entries[strRequirementOid]
    }

    override fun process(parent: StructuralNode, strTarget: String, attributes: MutableMap<String, Any>): Any? {

        val strVersion = attributes[DeprecationAttributes.VERSION.key]?.toString()
        checkNotNull(strVersion) {
            logger.error("$BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT missing required attribute '${DeprecationAttributes.VERSION.key}'")
        }

        val strVersionOid = makeConformityVersionOid(strVersion)

        val requirementNumber: Int = parseRequirementNumber(strTarget)
        val strRequirementId = getRequirementOid(requirementNumber)

        checkNotNull(!entries.containsKey(strRequirementId)) {
            logger.error("Requirement $strRequirementId is already marked deprecated")
        }

        entries[strRequirementId] = DeprecatedOid(strRequirementId, strVersionOid)

        return null
    }
}