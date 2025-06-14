package org.sdpi.asciidoc.model

data class BlockOwner(
    val title: String,
    val role: String?,
    val owner: BlockOwner?) {

    fun dump() {
        owner?.dump()
        print("\t${title} ($role)")
    }
}