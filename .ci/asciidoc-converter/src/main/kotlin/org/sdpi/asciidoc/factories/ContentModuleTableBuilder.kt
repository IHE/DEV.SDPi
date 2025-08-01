package org.sdpi.asciidoc.factories

import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.model.Obligation
import org.sdpi.asciidoc.model.SdpiActor
import org.sdpi.asciidoc.model.SdpiContentModule

class ContentModuleTableBuilder(
    private val processor: Treeprocessor,
    val table: Table,
) {
    private val colActor = processor.createTableColumn(table, 0)
    private val colModule = processor.createTableColumn(table, 1)
    private val colObligation = processor.createTableColumn(table, 2)
    private val colOption = processor.createTableColumn(table, 3 )
    private val infoCellStyles = mapOf("halign" to "center")

    fun setupHeadings() {
        val header = processor.createTableRow(table)
        table.header.add(header)

        header.cells.add(processor.createTableCell(colActor, "Actor"))
        header.cells.add(processor.createTableCell(colModule, "Content module"))
        header.cells.add(processor.createTableCell(colObligation, "Obligation", infoCellStyles))
        header.cells.add(processor.createTableCell(colOption, "Option", infoCellStyles))
    }

    fun addRow(
        actor: SdpiActor?,
        module: SdpiContentModule?,
        obligation: Obligation,
        strOption: String?
    ) {
        val strDefaultObligation = obligation.keyword

        val row = processor.createTableRow(table)
        row.cells.add(processor.createTableCell(colActor, createActorCell(actor)))
        row.cells.add(processor.createTableCell(colModule, createModuleCell(module)))
        row.cells.add(processor.createTableCell(colObligation, strDefaultObligation, infoCellStyles))
        row.cells.add(processor.createTableCell(colOption, strOption ?: "—", infoCellStyles))
        table.body.add(row)
    }

    private fun createActorCell(actor: SdpiActor?): String {
        if (actor == null) {
            return ""
        }

        return makeLink(actor.id, actor.label)
    }

    private fun createModuleCell(module: SdpiContentModule?): String {
        if (module == null) {
            return ""
        }

        return "${module.label} (${makeLink(module.anchor, module.label)})"
    }

    private fun makeLink(strAnchor: String, strText: String): String {
        return "link:#$strAnchor[$strText]"
    }

}