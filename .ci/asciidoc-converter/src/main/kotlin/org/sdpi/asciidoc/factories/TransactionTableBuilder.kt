package org.sdpi.asciidoc.factories

import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.model.Contribution
import org.sdpi.asciidoc.model.Obligation
import org.sdpi.asciidoc.model.StructuralNodeWrapper

class TransactionTableBuilder(private val processor: Treeprocessor, val table: Table) {
    private val colTransaction = processor.createTableColumn(table, 0)
    private val colContribution = processor.createTableColumn(table, 1)
    private val colObligation = processor.createTableColumn(table, 2)
    private val colOption = processor.createTableColumn(table, 3)
    private val infoCellStyles  = mapOf("halign" to "center")

    fun setupHeadings() {
        val header = processor.createTableRow(table)
        table.header.add(header)

        header.cells.add(processor.createTableCell(colTransaction, "Transaction"))
        header.cells.add(processor.createTableCell(colContribution, "Contribution", infoCellStyles))
        header.cells.add(processor.createTableCell(colObligation, "Obligation", infoCellStyles))
        header.cells.add(processor.createTableCell(colOption, "Option", infoCellStyles))

    }

    fun addRow(strTransactionCell: String, contribution: Contribution?, obligation: Obligation, strOption: String?) {
        val strContribution = contribution?.keyword ?: ""
        val strDefaultObligation = obligation.keyword

        val row = processor.createTableRow(table)
        row.cells.add(processor.createTableCell(colTransaction, strTransactionCell))
        row.cells.add(processor.createTableCell(colContribution, strContribution, infoCellStyles))
        row.cells.add(processor.createTableCell(colObligation, strDefaultObligation, infoCellStyles))
        row.cells.add(processor.createTableCell(colOption, strOption ?: "—", infoCellStyles))
        table.body.add(row)
    }
}