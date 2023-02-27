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
    fun run() {
        val actualIgnored = ignored.toMutableList().also {
            it.add(ReferenceSanitizerPreprocessor.refSeparator)
        }
        val errors = errorStream.toByteArray().decodeToString()
            .split("\n").count { line ->
                when (val errorPrefix = errorHints.firstOrNull { line.startsWith(it, true) }) {
                    null -> false
                    else -> if (actualIgnored.any { line.contains(it) }) {
                        logger.info {
                            "Asciidoc issue ignored: ${line.substring(errorPrefix.length)}"
                        }.let { false }
                    } else {
                        logger.error {
                            "Asciidoc issue detected: ${line.substring(errorPrefix.length)}"
                        }.let { true }
                    }
                }
            }

        if (errors > 0) {
            throw Exception("Found $errors Asciidoc error(s) that need to be fixed (see previous output)")
        }
    }

    private companion object : Logging {
        val errorHints = listOf("information: ", "info: ")
        val ignored = listOf<String>()
    }
}
