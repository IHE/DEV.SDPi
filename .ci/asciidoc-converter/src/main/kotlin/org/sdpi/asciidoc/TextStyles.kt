package org.sdpi.asciidoc

enum class LinkStyles(val className: String) {

    // Stops the link postprocessor replacing the title text
    // with section numbering.
    TITLE_TEXT("title"),
}