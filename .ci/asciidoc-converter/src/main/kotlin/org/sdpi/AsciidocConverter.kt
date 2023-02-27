package org.sdpi

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.sdpi.asciidoc.extension.*
import java.io.File
import java.io.OutputStream

class AsciidocConverter(
    private val inputType: Input,
    private val outputFile: File,
    private val mode: Mode = Mode.Productive
) : Runnable {
    override fun run() {
        val options = Options.builder()
            .safe(SafeMode.UNSAFE)
            .backend(BACKEND)
            .sourcemap(true)
            .toFile(outputFile).build()

        val asciidoctor = Asciidoctor.Factory.create()

        val anchorReplacements = mutableMapOf<String, LabelInfo>()
        val customReferences = mutableSetOf<String>()

        asciidoctor.javaExtensionRegistry().block(RequirementsBlockProcessor())
        asciidoctor.javaExtensionRegistry().treeprocessor(
            NumberingProcessor(
                when (mode) {
                    is Mode.Test -> mode.structureDump
                    else -> null
                },
                anchorReplacements
            )
        )
        asciidoctor.javaExtensionRegistry().treeprocessor(RequirementLevelProcessor())
        asciidoctor.javaExtensionRegistry().preprocessor(DisableSectNumsProcessor())
        asciidoctor.javaExtensionRegistry().preprocessor(ReferenceSanitizerPreprocessor())
        asciidoctor.javaExtensionRegistry()
            .postprocessor(ReferenceSanitizerPostprocessor(anchorReplacements, customReferences))

        asciidoctor.requireLibrary("asciidoctor-diagram") // enables plantuml
        when (inputType) {
            is Input.FileInput -> asciidoctor.convertFile(inputType.file, options)
            is Input.StringInput -> asciidoctor.convert(inputType.string, options)
        }

        asciidoctor.shutdown()
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