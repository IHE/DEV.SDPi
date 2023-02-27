package org.sdpi.asciidoc.extension

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.Preprocessor
import org.asciidoctor.extension.PreprocessorReader
import java.io.File

/**
 * Removes any occurrences of `:sectnums:` from the document to prevent the AscidoctorJ parser from rendering
 * additional section numbers.
 */
class DisableSectNumsProcessor() : Preprocessor() {
    override fun process(document: Document, reader: PreprocessorReader) {
        reader.readLines().filter {
            it.trim() != ":sectnums:"
        }.also {
            reader.restoreLines(it)
        }
    }

    private companion object {
        const val PLANTUML_SOURCES_FOLDER_NAME = "plantuml"

        private val plantUmlIncludeRegex = "^!include\\s+?(\\S.*)$".toRegex()
    }
}