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
    private val knownAnchors = mutableListOf<String>()

    fun getKnownAnchors(): List<String> = knownAnchors

    override fun process(document: Document): Document {

        processBlock(document)

        return document
    }

    fun dumpKnownAnchors() {
        println("Anchors found in the document:")
        for(strId in knownAnchors) {
            println("  #$strId")
        }
        println("Found ${knownAnchors.count()} unique anchors")
    }

    private fun processBlock(block: StructuralNode) {
        val strId = block.id
        if (strId != null) {
            if (knownAnchors.contains(strId)) {
                logger.error("Found duplicate id $strId; ids should be unique.")
            }
            knownAnchors.add(strId)
        }

        for (child in block.blocks) {
            processBlock(child)
        }
    }


}