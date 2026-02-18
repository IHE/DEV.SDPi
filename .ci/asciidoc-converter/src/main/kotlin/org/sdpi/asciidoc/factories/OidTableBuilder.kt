package org.sdpi.asciidoc.factories

import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.makeLink
import org.sdpi.asciidoc.model.SdpiOidReference

class OidTableBuilder(
    private val processor: Treeprocessor,
    val table: Table,
) {
    private val colOid = processor.createTableColumn(table, 0)
    private val colType = processor.createTableColumn(table, 1)
    private val colDescription = processor.createTableColumn(table, 2)

    fun setupHeadings() {
        val header= processor.createTableRow(table)
        table.header.add(header)

        header.cells.add(processor.createTableCell(colOid, "Oid"))
        header.cells.add(processor.createTableCell(colType, "Type"))
        header.cells.add(processor.createTableCell(colDescription, "Description"))

    }

    fun addRow(oid: SdpiOidReference) {

        val row = processor.createTableRow(table)

        row.cells.add(processor.createTableCell(colOid, makeLink(oid.anchor, oid.oid, true)))
        row.cells.add(processor.createTableCell(colType, oid.root.typeLabel))
        row.cells.add(processor.createTableCell(colDescription, oid.description))

        table.body.add(row)
    }
}