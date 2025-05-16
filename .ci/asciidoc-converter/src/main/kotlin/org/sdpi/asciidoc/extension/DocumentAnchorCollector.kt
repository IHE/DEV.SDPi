package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor

/**
 * Gathers a list of all the ids (anchors) in the document.
 */
class DocumentAnchorCollector : Treeprocessor() {

    private companion object : Logging

    // Collection of all the known anchors we find in the document to resolve
    // possible missing anchors reported by Ascii Doctor.
    private val knownAnchors = mutableMapOf<String, String>()

    fun getKnownAnchors(): Map<String, String> = knownAnchors

    override fun process(document: Document): Document {

        processBlock(document)

        return document
    }

    fun dumpKnownAnchors() {
        println("Anchors found in the document:")
        for(anchor in knownAnchors) {
            println("  #${anchor.key} => '${anchor.value}'")
        }
        println("Found ${knownAnchors.count()} unique anchors")
    }

    private fun processBlock(block: StructuralNode) {
        val strId = block.id
        val strRefText = block.attributes["reftext"] ?: block.title ?: strId
        if (strId != null) {
            if (knownAnchors.contains(strId)) {
                logger.error("Found duplicate id $strId; ids should be unique.")
            }
            knownAnchors[strId] = strRefText.toString()
        }


        for (child in block.blocks) {
            processBlock(child)
        }
    }


}