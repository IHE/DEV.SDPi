package org.sdpi

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.sdpi.asciidoc.extension.*
import org.sdpi.asciidoc.github.Issues
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

class AsciidocConverter(
    private val inputType: Input,
    private val outputFile: File,
    private val githubToken: String?,
    private val mode: Mode = Mode.Productive,
) : Runnable {
    override fun run() {
        val options = Options.builder()
            .safe(SafeMode.UNSAFE)
            .backend(BACKEND)
            .sourcemap(true)
            .toFile(outputFile).build()

        val asciidoctor = Asciidoctor.Factory.create()

        val anchorReplacements = AnchorReplacementsMap()

        // todo: load from somewhere, e.g. JSON file.
        val sourceSpecifications : Map<String, String> = mapOf(
            "sdpi" to "1.3.6.1.4.1.19376.1.6.2.10.1.1.1",
            "sdpi-p" to "1.3.6.1.4.1.19376.1.6.2.11",
            "sdpi-a" to "1.3.6.1.4.1.19376.1.6.2.x",
            "sdpi-r" to "1.3.6.1.4.1.19376.1.6.2.x",
            "sdpi-xC" to "1.3.6.1.4.1.19376.1.6.2.x",
        )

        val requirementsBlockProcessor = RequirementsBlockProcessor(sourceSpecifications)
        asciidoctor.javaExtensionRegistry().block(requirementsBlockProcessor)
        asciidoctor.javaExtensionRegistry().treeprocessor(
            NumberingProcessor(
                when (mode) {
                    is Mode.Test -> mode.structureDump
                    else -> null
                },
                anchorReplacements
            )
        )

        val requirementsListBlockMacroProcess = RequirementListMacroProcessor()
        val requirementsListProcessor = RequirementListProcessor(requirementsBlockProcessor)
        asciidoctor.javaExtensionRegistry().blockMacro(requirementsListBlockMacroProcess)
        asciidoctor.javaExtensionRegistry().treeprocessor(requirementsListProcessor)

        asciidoctor.javaExtensionRegistry().treeprocessor(RequirementLevelProcessor())
        asciidoctor.javaExtensionRegistry().preprocessor(IssuesSectionPreprocessor(githubToken))
        asciidoctor.javaExtensionRegistry().preprocessor(DisableSectNumsProcessor())
        asciidoctor.javaExtensionRegistry().preprocessor(ReferenceSanitizerPreprocessor(anchorReplacements))
        asciidoctor.javaExtensionRegistry()
            .postprocessor(ReferenceSanitizerPostprocessor(anchorReplacements))

        asciidoctor.requireLibrary("asciidoctor-diagram") // enables plantuml
        when (inputType) {
            is Input.FileInput -> asciidoctor.convertFile(inputType.file, options)
            is Input.StringInput -> asciidoctor.convert(inputType.string, options)
        }

        asciidoctor.shutdown()

        val referencedArtifactsName = "referenced-artifacts"
        val path = Path.of(outputFile.parentFile.absolutePath, referencedArtifactsName)
        Files.createDirectories(path)
        val reqsDump = Json.encodeToString(requirementsBlockProcessor.detectedRequirements())
        Path.of(path.toFile().absolutePath, "sdpi-requirements.json").toFile().writeText(reqsDump)
    }

    private companion object {
        const val BACKEND = "html"
    }

    sealed interface Input {
        data class FileInput(val file: File) : Input
        data class StringInput(val string: String) : Input
    }

    sealed interface Mode {
        object Productive : Mode
        data class Test(val structureDump: OutputStream) : Mode
    }
}