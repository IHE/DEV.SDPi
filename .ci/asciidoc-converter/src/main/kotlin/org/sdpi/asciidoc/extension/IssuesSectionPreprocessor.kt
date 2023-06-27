package org.sdpi.asciidoc.extension

import org.asciidoctor.extension.Preprocessor
import org.asciidoctor.extension.PreprocessorReader
import org.kohsuke.github.GHIssue
import org.sdpi.asciidoc.github.IssueImport
import org.sdpi.asciidoc.github.Issues


class IssuesSectionPreprocessor(
    private val githubToken: String?
) : Preprocessor() {

    override fun process(document: org.asciidoctor.ast.Document, reader: PreprocessorReader) {
        val lines = reader.readLines()
        if (githubToken == null) {
            reader.restoreLines(lines)
            return
        }

        val variables = lines
            .mapNotNull { VariableDeclaration.fromLine(it) }
            .filter { it.variableName == VAR_MILESTONE_PUBLICATION || it.variableName == VAR_MILESTONE_REVIEW }
            .associateBy(VariableDeclaration::variableName)

        val issues = IssueImport(
            githubToken,
            variables[VAR_MILESTONE_PUBLICATION]!!.variableValue,
            variables[VAR_MILESTONE_REVIEW]!!.variableValue
        ).requestIssues().issues()

        reader.restoreLines(lines.fold(mutableListOf<String>()) { acc, line ->

            when (line.trim().lowercase()) {
                INSTR_OPEN_ISSUES -> acc.apply { renderIssues(this, issues.openIssuesInMilestones) }
                INSTR_CLOSED_ISSUES -> acc.apply { renderIssues(this, issues.closedIssuesInMilestones) }
                INSTR_TOI_ISSUES -> acc.apply { renderIssues(this, issues.toiIssues) }
                else -> acc.apply { add(line) }
            }
        }.toMutableList())
    }

    private fun renderIssues(lines: MutableList<String>, issues: List<GHIssue>) {
        issues.forEach {
            lines.add("- ${it.htmlUrl}[${it.title}]")
        }
    }

    private companion object {
        const val INSTR_OPEN_ISSUES = "// open issues are inserted here"

        const val INSTR_CLOSED_ISSUES = "// closed issues are inserted here"

        const val INSTR_TOI_ISSUES = "// toi issues are inserted here"

        const val VAR_MILESTONE_PUBLICATION = "sdpi_milestone_publication"
        const val VAR_MILESTONE_REVIEW = "sdpi_milestone_review"
    }
}