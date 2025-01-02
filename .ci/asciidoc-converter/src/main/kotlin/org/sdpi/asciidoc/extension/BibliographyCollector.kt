package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.getLocation
import org.sdpi.asciidoc.model.BibliographyEntry

class BibliographyCollector() : Treeprocessor()
{
    private companion object : Logging

    private val biblographyEntries = mutableMapOf<String, BibliographyEntry>()

    fun bibliography() = biblographyEntries

    override fun process(document: Document): Document
    {
        collectBibliography(document)
        return document
    }

    private fun collectBibliography(document: Document) : Boolean
    {
        logger.info("Searching for bibliography")
        val bib = findBibliography(document as StructuralNode)
        checkNotNull(bib)
        {
            "Can't find bibliography".also { logger.error { it } }
        }

        val reBibParser = Regex("id=\"(?<ref>.+)\".*\\[(?<reftxt>.+)]\\s+(?<entry>.+)")

        // Expecting first child to be a list of bibliography entries.
        when (val bibList = bib.blocks[0])
        {
            is org.asciidoctor.ast.List ->
            {
                for(bibEntry in bibList.items)
                {
                    if (bibEntry is org.asciidoctor.ast.ListItem)
                    {
                        val strItem = bibEntry.text
                        val mParsed = reBibParser.find(strItem)
                        checkNotNull(mParsed)
                        {
                            "${getLocation(bibEntry)} invalid format '$strItem'".also { logger.error{it} }
                        }
                        val strRef = mParsed.groups["ref"]?.value
                        checkNotNull(strRef)
                        {
                            "${getLocation(bibEntry)} missing reference".also { logger.error{it} }
                        }
                        val strRefText = mParsed.groups["reftxt"]?.value
                        checkNotNull(strRefText)
                        {
                            "${getLocation(bibEntry)} missing reference text for $strRef".also { logger.error{it} }
                        }
                        val strSource = mParsed.groups["entry"]?.value
                        checkNotNull(strSource)
                        {
                            "${getLocation(bibEntry)} missing reference source for $strRef".also { logger.error{it} }
                        }

                        if (biblographyEntries.contains(strRef))
                        {
                            "${getLocation(bibEntry)} duplicate reference id $strRef".also { logger.error{it} }
                        }
                        biblographyEntries[strRef] = BibliographyEntry(strRef, strRefText, strSource)
                    }
                }
            }
            else ->
            {
                "Can't find bibliography entries".also { logger.error { it } }
            }
        }

        logger.info("Found ${biblographyEntries.count()} bibliography entries")

        return true
    }

    private fun findBibliography(block : StructuralNode): StructuralNode?
    {
        //logger.info("Searching ${block.sourceLocation.lineNumber}")
        // The bibliography must be a section and have the bibliography style (see
        // https://docs.asciidoctor.org/asciidoc/latest/sections/bibliography/#bibliography-section-syntax)
        if (block.context == "section")
        {
            val strStyle = block.attributes["style"]?.toString()
            if (strStyle != null && strStyle == "bibliography")
            {
                return block
            }
        }

        for(child in block.blocks)
        {
            val bib = findBibliography(child)
            if (bib != null)
            {
                return bib
            }
        }

        return null
    }


}