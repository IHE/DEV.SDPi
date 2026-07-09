package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.DeprecationAttributes
import org.sdpi.asciidoc.model.DeprecatedOid
import org.sdpi.asciidoc.model.WellKnownOid
import org.sdpi.asciidoc.model.makeConformityVersionOid

const val BLOCK_MACRO_NAME_DEPRECATE = "Deprecate"
const val BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT = "DeprecateRequirement"
const val BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION = "DeprecateTransaction"

@Name(BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT)
class DeprecateRequirementProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT) {
    private companion object : Logging

    val entries: Map<String, DeprecatedOid>
        field = mutableMapOf()

    fun isDeprecated(strRequirementOid: String) : DeprecatedOid? {
        return entries[strRequirementOid]
    }

    override fun process(parent: StructuralNode, strTarget: String, attributes: MutableMap<String, Any>): Any? {

        val strVersion = attributes[DeprecationAttributes.VERSION.key]?.toString()
        checkNotNull(strVersion) {
            logger.error("$BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT missing required attribute '${DeprecationAttributes.VERSION.key}'")
        }

        val strVersionOid = makeConformityVersionOid(strVersion)

        val requirementNumber = parseRequirementNumber(strTarget)
        val strRequirementId = getRequirementOid(requirementNumber)

        checkNotNull(!entries.containsKey(strRequirementId)) {
            logger.error("Requirement $strRequirementId is already marked deprecated")
        }

        entries[strRequirementId] = DeprecatedOid(strRequirementId, strVersionOid)

        return null
    }
}
@Name(BLOCK_MACRO_NAME_DEPRECATE)
class DeprecateProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_DEPRECATE) {
    private companion object : Logging

    val entries: Map<String, DeprecatedOid>
        field = mutableMapOf()

    fun isDeprecated(strOid: String) : DeprecatedOid? {
        return entries[strOid]
    }

    override fun process(parent: StructuralNode, strTarget: String, attributes: MutableMap<String, Any>): Any? {

        val strVersion = attributes[DeprecationAttributes.VERSION.key]?.toString()
        checkNotNull(strVersion) {
            "$BLOCK_MACRO_NAME_DEPRECATE_REQUIREMENT missing required attribute '${DeprecationAttributes.VERSION.key}'".also {
                logger.error{it}
            }
        }
        println("**Deprecating $strTarget")
        val strVersionOid = makeConformityVersionOid(strVersion)


        check(strTarget.startsWith(WellKnownOid.DEV_SDPi.oid)) {
            "Oid `$strTarget` must begin with ${WellKnownOid.DEV_SDPi.oid}".also {
                logger.error{it}
            }
        }

        val reOid = Regex("""^(?:(?:[01]\.(?:[0-9]|[1-3][0-9]))|(?:2\.(?:0|[1-9]\d*)))(?:\.(?:0|[1-9]\d*))*$""")
        check(reOid.matches(strTarget)) {
            "Oid `$strTarget` must be a valid oid".also {
                logger.error{it}
            }
        }

        checkNotNull(!entries.containsKey(strTarget)) {
            "Oid $strTarget is already marked deprecated".also {
                logger.error{it}
            }
        }


        entries[strTarget] = DeprecatedOid(strTarget, strVersionOid)

        return null
    }
}

@Name(BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION)
class DeprecateTransactionProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION) {
    private companion object : Logging

    val entries: Map<String, DeprecatedOid>
        field = mutableMapOf()

    fun isDeprecated(strTransactionOid: String) : DeprecatedOid? {
        return entries[strTransactionOid]
    }

    override fun process(parent: StructuralNode, strTarget: String, attributes: MutableMap<String, Any>): Any? {

        val strVersion = attributes[DeprecationAttributes.VERSION.key]?.toString()
        checkNotNull(strVersion) {
            "$BLOCK_MACRO_NAME_DEPRECATE_TRANSACTION missing required attribute '${DeprecationAttributes.VERSION.key}'".also {
                logger.error{it}
            }
        }

        val strVersionOid = makeConformityVersionOid(strVersion)

        val strTransactionId = getTransactionOid(strTarget)

        checkNotNull(!entries.containsKey(strTransactionId)) {
            "Transaction $strTransactionId is already marked deprecated".also {
                logger.error{it}
            }
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
        throw IllegalArgumentException("'$strTransactionId' is not a valid transaction id")
    }
}