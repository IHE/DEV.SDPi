package org.sdpi.asciidoc.factories

import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.makeLink
import org.sdpi.asciidoc.model.*

class TransactionTableBuilder(
    private val processor: Treeprocessor,
    val table: Table,
    private val bIncludeActorColumn: Boolean
) {
    private val colActor = if (bIncludeActorColumn) processor.createTableColumn(table, 0) else null
    private val colTransaction = processor.createTableColumn(table, 0 + if (bIncludeActorColumn) 1 else 0)
    private val colContribution = processor.createTableColumn(table, 1 + if (bIncludeActorColumn) 1 else 0)
    private val colObligation = processor.createTableColumn(table, 2 + if (bIncludeActorColumn) 1 else 0)
    private val colOption = processor.createTableColumn(table, 3 + if (bIncludeActorColumn) 1 else 0)
    private val infoCellStyles = mapOf("halign" to "center")

    fun setupHeadings() {
        val header = processor.createTableRow(table)
        table.header.add(header)

        if (bIncludeActorColumn) {
            header.cells.add(processor.createTableCell(colActor, "Actor"))
        }

        header.cells.add(processor.createTableCell(colTransaction, "Transaction"))
        header.cells.add(processor.createTableCell(colContribution, "Contribution", infoCellStyles))
        header.cells.add(processor.createTableCell(colObligation, "Obligation", infoCellStyles))
        header.cells.add(processor.createTableCell(colOption, "Option", infoCellStyles))

    }

    fun addRow(
        actor: SdpiActor?,
        transaction: SdpiTransaction?,
        contribution: Contribution?,
        obligation: Obligation,
        option: OptionId?
    ) {
        val strContribution = contribution?.keyword ?: ""
        val strDefaultObligation = obligation.keyword

        val row = processor.createTableRow(table)
        if (bIncludeActorColumn) {
            row.cells.add(processor.createTableCell(colActor, createActorCell(actor)))
        }


        row.cells.add(processor.createTableCell(colTransaction, createTransactionCell(transaction)))
        row.cells.add(processor.createTableCell(colContribution, strContribution, infoCellStyles))
        row.cells.add(processor.createTableCell(colObligation, strDefaultObligation, infoCellStyles))
        row.cells.add(processor.createTableCell(colOption, createOptionCell(option), infoCellStyles))
        table.body.add(row)
    }

    private fun createActorCell(actor: SdpiActor?): String {
        if (actor == null) {
            return ""
        }

        return makeLink(actor.id, actor.label)
    }

    private fun createTransactionCell(transaction: SdpiTransaction?): String {
        if (transaction == null) {
            return ""
        }

        val strTransactionLink = makeLink(transaction.anchor, transaction.label)
        val strTransactionIdLink = transaction.createTransactionListLink()
        return "$strTransactionLink ($strTransactionIdLink)"
    }

    private fun createOptionCell(option: OptionId?): String {
        if (option == null) {
            return "—"
        }
        val strOptionLink = makeLink(option.anchor, option.label)
        return strOptionLink
    }

}