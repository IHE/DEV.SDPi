package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.extension.Preprocessor
import org.asciidoctor.extension.PreprocessorReader
import java.net.URLEncoder
import java.util.*


class ReferenceSanitizerPreprocessor(
    private val anchorReplacements: MutableMap<String, LabelInfo>
) : Preprocessor() {
    private val variables = mutableMapOf<String, String>()

    override fun process(document: org.asciidoctor.ast.Document, reader: PreprocessorReader) {
        val lines = reader.readLines()

        reader.restoreLines(lines.map { line ->
            manageVariables(line)
            referenceMatcher.replace(line) {
                val refs = it.groupValues[1].split(",", limit = 2)
                if (refs.size < 2) {
                    it.groupValues.first()
                } else {
                    val substitutedVariables = substituteVariables(refs[1].trim())
                    val encodedRefText = URLEncoder.encode(substitutedVariables, Charsets.UTF_8)
                    val transformed = "<<${refs[0]}$refSeparator$encodedRefText>>"
                    anchorReplacements[refs[0]] = LabelInfo(substitutedVariables, LabelSource.UNKNOWN)
                    logger.info { "Found reference with custom label: ${it.groupValues.first()} => $transformed" }
                    transformed
                }
            }
        }.toMutableList())
    }

    private fun manageVariables(line: String) {
        val match = variableDeclarationMatcher.find(line)
        if (match != null) {
            if (match.groupValues[1] == "!") {
                variables.remove(match.groupValues[2])
            } else {
                variables[match.groupValues[2]] = match.groupValues[3].trim()
            }
        }
    }

    private fun substituteVariables(text: String) = variableReferenceMatcher.replace(text) {
        variables[it.groupValues[1]] ?: "[${it.groupValues.first()}]"
    }


    companion object : Logging {
        private val referenceMatcher = """<<(.+?)>>""".toRegex()
        private val variableDeclarationMatcher = """^:(!?)(.+?):(.*)$""".toRegex()
        private val variableReferenceMatcher = """\{(.+?)}""".toRegex()
        val refSeparator = UUID.randomUUID().toString()
    }
}