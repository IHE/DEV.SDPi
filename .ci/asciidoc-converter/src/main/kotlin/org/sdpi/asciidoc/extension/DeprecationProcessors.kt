package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.jruby.util.ResourceException.InvalidArguments
import org.sdpi.asciidoc.DeprecationAttributes
import org.sdpi.asciidoc.model.DeprecatedOid
import org.sdpi.asciidoc.model.WellKnownOid
import org.sdpi.asciidoc.model.makeConformityVersionOid

const val BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT = "DeprecateRequirement"
const val BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION = "DeprecateTransaction"

@Name(BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT)
class DeprecateRequirementProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT) {
    private companion object : Logging

    private val entries = mutableMapOf<String, DeprecatedOid>()

    fun entries() : Map<String, DeprecatedOid> {
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
@Name(BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION)
class DeprecateTransactionProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION) {
    private companion object : Logging

    private val entries = mutableMapOf<String, DeprecatedOid>()

    fun entries() : Map<String, DeprecatedOid> {
        return entries
    }

    fun isDeprecated(strTransactionOid: String) : DeprecatedOid? {
        return entries[strTransactionOid]
    }

    override fun process(parent: StructuralNode, strTarget: String, attributes: MutableMap<String, Any>): Any? {

        val strVersion = attributes[DeprecationAttributes.VERSION.key]?.toString()
        checkNotNull(strVersion) {
            logger.error("$BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION missing required attribute '${DeprecationAttributes.VERSION.key}'")
        }

        val strVersionOid = makeConformityVersionOid(strVersion)

        val strTransactionId = getTransactionOid(strTarget)

        checkNotNull(!entries.containsKey(strTransactionId)) {
            logger.error("Transaction $strTransactionId is already marked deprecated")
        }

        entries[strTransactionId] = DeprecatedOid(strTransactionId, strVersionOid)

        return null
    }

    private fun getTransactionOid(strTransactionId: String): String {
        if (strTransactionId.startsWith("DEV-")) {
            val strLeaf = strTransactionId.substring(4)
            if (strLeaf.toIntOrNull() != null) {
                return "${WellKnownOid.DEV_TRANSACTION.oid}.$strLeaf"
            }
        }
        throw InvalidArguments("'$strTransactionId' is not a valid transaction id")
    }
}