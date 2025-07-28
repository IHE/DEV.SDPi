package org.sdpi.asciidoc

import org.apache.logging.log4j.kotlin.Logging
import org.sdpi.asciidoc.extension.ReferenceSanitizerPreprocessor
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Checks for Asciidoc error messages.
 *
 * Starts capturing the process' error stream when initialized, seeks Asciidoc error messages and throws if at least one is found.
 */
class AsciidocErrorChecker {
    private val errorStream = ByteArrayOutputStream().also {
        System.setErr(PrintStream(it))
    }

    /**
     * Runs the check on the latest captured error stream.
     */
    fun run(knownAnchors: List<String>) {
        val actualIgnored = ignored.toMutableList().also {
            it.add(ReferenceSanitizerPreprocessor.refSeparator)
        }
        val errors = errorStream.toByteArray().decodeToString()
            .split("\n").count { line ->
                when (val errorPrefix = errorHints.firstOrNull { line.startsWith(it, true) }) {
                    null -> false
                    else -> if (actualIgnored.any { line.contains(it) }) {
                        logger.info {
                            "Asciidoc issue ignored (includes magic value): ${line.substring(errorPrefix.length)}"
                        }.let { false }
                    } else {
                        val invalidRefMatch = reInvalidReference.findAll(line).map {it.groupValues[1]}
                        if (invalidRefMatch.any { it in knownAnchors }) {
                            logger.info {
                                "Asciidoc issue ignored (anchor is defined): ${line.substring(errorPrefix.length)}"
                            }.let { false }
                        } else {
                            logger.error {
                                "Asciidoc issue detected: ${line.substring(errorPrefix.length)}"
                            }.let { true }
                        }
                    }
                }
            }

        if (errors > 0) {
            throw Exception("Found $errors Asciidoc error(s) that need to be fixed (see previous output)")
        }
    }

    private companion object : Logging {
        val errorHints = listOf("information: ", "info: ")
        val reInvalidReference = "possible invalid reference: ([a-zA-Z0-9_]+)".toRegex()
        val ignored = listOf<String>()
    }
}
