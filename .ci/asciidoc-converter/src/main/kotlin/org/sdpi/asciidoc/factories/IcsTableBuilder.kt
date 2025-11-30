package org.sdpi.asciidoc.factories

import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.model.RequirementLevel

class IcsTableBuilder (
    private val processor: Treeprocessor,
    val table: Table,
){

    private val colId = processor.createTableColumn(table, 0)
    private val colReference = processor.createTableColumn(table, 1)
    private val colStatus = processor.createTableColumn(table, 2)
    private val colSupport = processor.createTableColumn(table, 3)
    private val colComment = processor.createTableColumn(table, 4)

    fun setupHeadings() {
        val header= processor.createTableRow(table)
        table.header.add(header)

        header.cells.add(processor.createTableCell(colId, "Index"))
        header.cells.add(processor.createTableCell(colReference, "Reference"))
        header.cells.add(processor.createTableCell(colStatus, "Status"))
        header.cells.add(processor.createTableCell(colSupport, "Support"))
        header.cells.add(processor.createTableCell(colComment, "Comment"))
    }

    fun addRow(strIcsId: String, strReference: String, level: RequirementLevel) {
        val row = processor.createTableRow(table)
        row.cells.add(processor.createTableCell(colId, strIcsId))
        row.cells.add(processor.createTableCell(colReference, strReference))
        row.cells.add(processor.createTableCell(colStatus, level.icsStatus))
        row.cells.add(processor.createTableCell(colSupport, ""))
        row.cells.add(processor.createTableCell(colComment, ""))

        table.body.add(row)
    }
}