package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.Postprocessor
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*

enum class LabelSource {
    SECTION,
    TABLE_OR_FIGURE,
    APPENDIX,
    VOLUME
}

data class LabelInfo(
    val label: String,
    val source: LabelSource,
    val prefix: String = "",
    val refText: String? = null
)

class ReferenceSanitizerPostprocessor(
    private val anchorLabels: Map<String, LabelInfo>,
    private val customReferences: Set<String>
) : Postprocessor() {
    override fun process(document: Document, output: String): String {
        // skip numbering if xref style has been changed to reduce likelihood of broken references
        if ((document.attributes[OPTION_XREFSTYLE] ?: DEFAULT_XREF) != DEFAULT_XREF) {
            return output
        }

        val sectionSig =
            sanitizeSig(document.attributes[OPTION_ASCIIDOC_SECTION_REFSIG]?.toString()?.trim() ?: "Section")
        val appendixSig =
            sanitizeSig(document.attributes[OPTION_ASCIIDOC_APPENDIX_REFSIG]?.toString()?.trim() ?: "Appendix")
        val chapterSig =
            sanitizeSig(document.attributes[OPTION_ASCIIDOC_CHAPTER_REFSIG]?.toString()?.trim().let {
                if (it == "Chapter") {
                    "Volume"
                } else {
                    it
                }
            } ?: "Volume")

        val doc = Jsoup.parse(output, "UTF-8")

        val anchors = doc.getElementsByTag("a")

        for (anchor in anchors) {
            if (isInToc(anchor)) {
                continue
            }

            val href = anchor.attr("href") ?: ""
            if (!href.startsWith("#")) {
                continue
            }

            val fragment = URI.create(href).fragment
            if (fragment.isNullOrEmpty()) {
                continue
            }

            val (id, label) = if (fragment.contains(ReferenceSanitizerPreprocessor.refSeparator)) {
                val parts = fragment.split(ReferenceSanitizerPreprocessor.refSeparator)
                Pair(parts[0], URLDecoder.decode(parts[1], Charsets.UTF_8).trim()).also { (id, label) ->
                    logger.info { "Found custom reference: $id => $label"}
                }
            } else {
                logger.info { "Found regular reference: $fragment"}
                Pair(fragment, null)
            }

            anchor.attr("href", "#$id")
            anchorLabels[id]?.also {
                val anchorText = it.refText ?: label
                when (it.source) {
                    LabelSource.SECTION -> anchor.text(anchorText ?: "$sectionSig${it.label}")
                    LabelSource.TABLE_OR_FIGURE -> anchor.text(anchorText ?: it.label)
                    LabelSource.APPENDIX -> anchor.text(anchorText ?: "$appendixSig${it.prefix}:${it.label}")
                    LabelSource.VOLUME -> anchor.text(anchorText ?: "$chapterSig${it.prefix}")
                }
            }
        }

        return doc.html()
    }

    private fun sanitizeSig(sig: String) = when (sig.isEmpty()) {
        true -> ""
        false -> "$sig "
    }

    private fun isInToc(element: Element) = element.parents().firstOrNull { it.id() == "toc" } != null

    private companion object : Logging {
        const val OPTION_ASCIIDOC_SECTION_REFSIG = "section-refsig"
        const val OPTION_ASCIIDOC_CHAPTER_REFSIG = "chapter-refsig"
        const val OPTION_ASCIIDOC_APPENDIX_REFSIG = "appendix-refsig"
        const val OPTION_XREFSTYLE = "xrefstyle"
        const val DEFAULT_XREF = "short"
    }
}