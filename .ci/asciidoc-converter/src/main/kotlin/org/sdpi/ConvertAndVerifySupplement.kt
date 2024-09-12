package org.sdpi

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import org.apache.logging.log4j.kotlin.Logging
import org.sdpi.asciidoc.AsciidocErrorChecker
import org.sdpi.asciidoc.github.IssueImport
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = ConvertAndVerifySupplement().main(args
//    when (System.getenv().containsKey("CI")) {
//        true -> args.firstOrNull()?.split(" ") ?: listOf() // caution: blanks in quotes not covered here!
//        false -> args.toList()
//    }
)

class ConvertAndVerifySupplement : CliktCommand("convert-supplement") {
    private companion object : Logging

    // for some reason, GitHub actions do not digest double quotes correctly right now - requires hard coded config
    private val adocInputFile by option("--input-file", help = "path to ascii doc input file")
        .file()
        .required()
        .validate {
            require(it.exists()) { "Input file '$it' does not exist." }
        }

    private val outputFolder by option("--output-folder", help = "path to artifact doc output folder")
        .file()
        .required()
        .validate {
            require(it.absoluteFile.parentFile.exists()) { "Output parent folder '${it.parentFile.absolutePath}' does not exist." }
            if (!it.exists()) {
                require(it.mkdir()) { "Output folder '${it.absolutePath}' could not be created" }
            }
        }

    private val backend by option("--backend", help = "'html' (default) or 'pdf' to set output type")
        .choice("pdf", "html", ignoreCase = true)
        .default("html")

    private val githubToken by option("--github-token", help = "Github token to request issues")

    override fun run() {
        runCatching {
            val asciidocErrorChecker = AsciidocErrorChecker()

            logger.info { "Start conversion of '${adocInputFile.canonicalPath}'" }

            val outFile = File(
                outputFolder.absolutePath + File.separator + adocInputFile.nameWithoutExtension + ".$backend"
            )

            logger.info { "Write output to '${outFile.canonicalPath}'" }

            AsciidocConverter(AsciidocConverter.Input.FileInput(adocInputFile), outFile, githubToken).run()

            asciidocErrorChecker.run()

            logger.info { "File successfully written" }
        }.onFailure {
            logger.error { it.message }
            logger.trace(it) { it.message }
            exitProcess(1)
        }
    }
}

