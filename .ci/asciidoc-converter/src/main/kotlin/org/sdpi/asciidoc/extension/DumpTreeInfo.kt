package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor

/**
 * Writes information about the tree to stdio to aid understanding
 * of document structure.
 */
class DumpTreeInfo : Treeprocessor() {
    private companion object : Logging

    override fun process(document: Document): Document {
        dumpBlock(document as StructuralNode, 0)
        return document
    }

    private fun dumpBlock(block: StructuralNode, nLevel: Int) {
        val sbBlockInfo = StringBuilder()
        val strIndent = "  ".repeat(nLevel)
        sbBlockInfo.append("${block.sourceLocation} > ")
        sbBlockInfo.append(strIndent)
        sbBlockInfo.append("${block.context}/${block.style}")
        sbBlockInfo.append(" [${getRoles(block)}]")
        sbBlockInfo.append(" '${shortTitle(block)}'")
        sbBlockInfo.append(" #${block.id}")
        //sbBlockInfo.append(" %${block.javaClass}%")
        sbBlockInfo.append(" {${getAttributes(block)}}")
        logger.debug { sbBlockInfo.toString() }

        block.blocks.forEach { dumpBlock(it, nLevel + 1) }
    }

    private fun getRoles(block: StructuralNode): String {
        var strRoles = ""
        for (strRole in block.roles) {
            strRoles += " '${strRole}'"
        }
        return strRoles
    }

    private fun getAttributes(block: StructuralNode): String {
        var strAttributes = ""

        for (strAttr in block.attributes.keys) {
            strAttributes += " '${strAttr}' => '${block.attributes[strAttr]}'"
        }

        return strAttributes
    }

    private fun shortTitle(block: StructuralNode): String {
        if (block.title == null) {
            return "---"
        }
        val nMaxLength = 20
        val strTitle: String = block.title
        if (strTitle.length <= nMaxLength) {
            return strTitle
        }
        return "${strTitle.substring(0, nMaxLength - 1)}..."
    }
}