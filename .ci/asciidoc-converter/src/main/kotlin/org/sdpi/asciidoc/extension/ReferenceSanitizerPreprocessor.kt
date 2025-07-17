package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.extension.Preprocessor
import org.asciidoctor.extension.PreprocessorReader
import java.net.URLEncoder
import java.util.*

class AnchorReplacementsMap {
    private val anchorReplacements: MutableMap<String, MutableMap<String, LabelInfo>> = mutableMapOf()

    fun put(firstLevel: String, secondLevel: String, value: LabelInfo) {
        val secondLevelMap = anchorReplacements[firstLevel].let {
            if (it == null) {
                anchorReplacements[firstLevel] = mutableMapOf()
            }
            anchorReplacements[firstLevel]!!
        }
        secondLevelMap[secondLevel] = value
    }

    fun put(firstLevel: String, value: LabelInfo) {
        put(firstLevel, defaultKey, value)
    }

    fun get(firstLevel: String): LabelInfo? {
        return get(firstLevel, defaultKey)
    }

    fun get(firstLevel: String, secondLevel: String): LabelInfo? {
        return anchorReplacements[firstLevel]?.let { it[secondLevel] }
    }

    fun dump() {
        logger.info("Anchor replacement map")
        logger.info("======================")
        for (entry in anchorReplacements) {
            logger.info(entry.key)
            for(entry2 in entry.value) {
                logger.info("  ${entry2.key} => ${entry2.value.label}/${entry2.value.source}/${entry2.value.prefix}/${entry2.value.refText}")
            }
        }
    }

    private companion object : Logging {
        val defaultKey = UUID.randomUUID().toString()
    }
}

class ReferenceSanitizerPreprocessor(
    private val anchorReplacements: AnchorReplacementsMap
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
                    val key = "${refs[0]}$refSeparator$encodedRefText"
                    val transformed = "<<$key>>"

                    anchorReplacements.put(
                        refs[0],
                        encodedRefText,
                        LabelInfo(substitutedVariables, LabelSource.UNKNOWN)
                    )
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
        private val variableDeclarationMatcher = VariableDeclarationRegex()
        private val variableReferenceMatcher = """\{(.+?)}""".toRegex()
        val refSeparator = UUID.randomUUID().toString()
    }
}