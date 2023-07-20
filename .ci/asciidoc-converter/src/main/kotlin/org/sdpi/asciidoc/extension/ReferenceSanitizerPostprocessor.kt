package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.Postprocessor
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URLDecoder

enum class LabelSource {
    SECTION,
    TABLE_OR_FIGURE,
    APPENDIX,
    VOLUME,
    UNKNOWN
}

data class LabelInfo(
    val label: String,
    val source: LabelSource,
    val prefix: String = "",
    val refText: String? = null
)

class ReferenceSanitizerPostprocessor(
    private val anchorLabels: AnchorReplacementsMap
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


            val parsedFragment = URI.create(href).fragment
            if (parsedFragment.isNullOrEmpty()) {
                continue
            }

            val rawFragment = href.substring(1)
            val (id, encodedLabel) = if (rawFragment.contains(ReferenceSanitizerPreprocessor.refSeparator)) {
                val parts = rawFragment.split(ReferenceSanitizerPreprocessor.refSeparator)
                Pair(parts[0], parts[1]).also { (id, label) ->
                    val decoded = decodeLabel(label)
                    logger.info { "Found custom reference: $id => $label => $decoded" }
                }
            } else {
                logger.info { "Found regular reference: $rawFragment" }
                Pair(rawFragment, null)
            }

            anchor.attr("href", "#$id")
            val item = encodedLabel?.let { anchorLabels.get(id, it) } ?: anchorLabels.get(id) ?: continue

            val anchorText = (encodedLabel?.let { decodeLabel(it) }) ?: item.refText
            when (item.source) {
                LabelSource.SECTION -> anchor.text(anchorText ?: "$sectionSig${item.prefix}:${item.label}")
                LabelSource.TABLE_OR_FIGURE -> anchor.text(anchorText ?: item.label)
                LabelSource.APPENDIX -> anchor.text(anchorText ?: "$appendixSig${item.prefix}:${item.label}")
                LabelSource.VOLUME -> anchor.text(anchorText ?: "$chapterSig${item.prefix}")
                LabelSource.UNKNOWN -> anchor.text(anchorText ?: item.label)
            }
        }

        return doc.html()
    }

    private fun decodeLabel(encodedLabel: String) = URLDecoder.decode(encodedLabel, Charsets.UTF_8).trim()

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