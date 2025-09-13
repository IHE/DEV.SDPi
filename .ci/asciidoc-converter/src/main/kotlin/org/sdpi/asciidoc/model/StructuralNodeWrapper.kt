package org.sdpi.asciidoc.model

import org.asciidoctor.ast.*
import org.sdpi.asciidoc.extension.BLOCK_NAME_SDPI_REQUIREMENT

/**
 * Creates a [StructuralNodeWrapper] from a structural node.
 */
fun StructuralNode.toSealed(): StructuralNodeWrapper {
    // this "when" will grow as other blocks need to be handled
    return when (this.context) {
        "section" -> StructuralNodeWrapper.Section(this as Section)
        "document" -> StructuralNodeWrapper.Document(this as Document)
        "paragraph" -> StructuralNodeWrapper.Paragraph(this as Block)
        "image" -> StructuralNodeWrapper.Image(this as Block)
        "table" -> StructuralNodeWrapper.Table(this as Table)
        "listing" -> StructuralNodeWrapper.Listing(this as Block)
        "sidebar" -> this.attributes.entries.find {
            it.key == "1" && it.value == BLOCK_NAME_SDPI_REQUIREMENT
        }?.let {
            StructuralNodeWrapper.SdpiRequirement(this as Block)
        } ?: StructuralNodeWrapper.Sidebar(this as Block)

        else -> StructuralNodeWrapper.Unknown
    }
}

/**
 * Wrapper class for improved functional dispatching.
 */
sealed class StructuralNodeWrapper {
    data class Section(val wrapped: org.asciidoctor.ast.Section) : StructuralNodeWrapper()
    data class Document(val wrapped: org.asciidoctor.ast.Document) : StructuralNodeWrapper()
    data class Sidebar(val wrapped: Block) : StructuralNodeWrapper()
    data class SdpiRequirement(val wrapped: Block) : StructuralNodeWrapper()
    data class Paragraph(val wrapped: Block) : StructuralNodeWrapper()
    data class Image(val wrapped: Block) : StructuralNodeWrapper()
    data class Table(val wrapped: org.asciidoctor.ast.Table) : StructuralNodeWrapper()
    data class Listing(val wrapped: Block) : StructuralNodeWrapper()
    object Unknown : StructuralNodeWrapper()
}

