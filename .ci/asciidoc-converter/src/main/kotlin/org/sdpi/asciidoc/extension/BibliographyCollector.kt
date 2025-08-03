package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.getLocation
import org.sdpi.asciidoc.model.BibliographyEntry

class BibliographyCollector : Treeprocessor() {
    private companion object : Logging

    private val bibliographyEntries = mutableMapOf<String, BibliographyEntry>()

    override fun process(document: Document): Document {
        collectBibliography(document)
        return document
    }

    fun findEntry(strRef: String): BibliographyEntry? {
        return bibliographyEntries[strRef]
    }

    private fun collectBibliography(document: Document): Boolean {
        logger.info("Searching for bibliography")
        val bib = findBibliography(document as StructuralNode)
        if (bib == null) {
            logger.warn("Can't find bibliography")
            return false
        }

        val reBibParser = Regex("\\[{3}(?<ref>\\w+)(,(?<reftxt>.+))?]{3}\\s+(?<entry>.+)")

        val bibEntries = findBibliographyEntries(bib)
        if (bibEntries == null) {
            logger.warn("Can't find biliography entries")
            return false
        }


        for (bibEntry in bibEntries.items) {
            if (bibEntry is org.asciidoctor.ast.ListItem) {
                val strItem = bibEntry.source
                val mParsed = reBibParser.find(strItem)
                checkNotNull(mParsed)
                {
                    "${getLocation(bibEntry)} invalid format '$strItem'".also { logger.error { it } }
                }
                val strRef = mParsed.groups["ref"]?.value
                checkNotNull(strRef)
                {
                    "${getLocation(bibEntry)} missing reference".also { logger.error { it } }
                }
                val strRefText = mParsed.groups["reftxt"]?.value
                checkNotNull(strRefText)
                {
                    "${getLocation(bibEntry)} missing reference text for $strRef".also { logger.error { it } }
                }
                val strSource = mParsed.groups["entry"]?.value
                checkNotNull(strSource)
                {
                    "${getLocation(bibEntry)} missing reference source for $strRef".also { logger.error { it } }
                }

                if (bibliographyEntries.contains(strRef)) {
                    "${getLocation(bibEntry)} duplicate reference id $strRef".also { logger.error { it } }
                }
                bibliographyEntries[strRef] = BibliographyEntry(strRef, strRefText, strSource)
            }
        }

        logger.info("Found ${bibliographyEntries.count()} bibliography entries")

        return true
    }

    private fun findBibliographyEntries(bib: StructuralNode): org.asciidoctor.ast.List? {
        for (child in bib.blocks) {
            if (child is org.asciidoctor.ast.List) {
                return child
            }
        }

        return null
    }

    private fun findBibliography(block: StructuralNode): StructuralNode? {
        //logger.info("Searching ${block.sourceLocation.lineNumber}")
        // The bibliography must be a section and have the bibliography style (see
        // https://docs.asciidoctor.org/asciidoc/latest/sections/bibliography/#bibliography-section-syntax)
        if (block.context == "section") {
            val strStyle = block.attributes["style"]?.toString()
            if (strStyle != null && strStyle == "bibliography") {
                return block
            }
        }

        for (child in block.blocks) {
            val bib = findBibliography(child)
            if (bib != null) {
                return bib
            }
        }

        return null
    }


}