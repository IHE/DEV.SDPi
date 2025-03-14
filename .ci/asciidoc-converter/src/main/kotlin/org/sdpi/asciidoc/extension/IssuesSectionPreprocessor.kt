package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.extension.Preprocessor
import org.asciidoctor.extension.PreprocessorReader
import org.kohsuke.github.GHIssue
import org.sdpi.asciidoc.github.IssueImport

class IssuesSectionPreprocessor(
    private val githubToken: String?
) : Preprocessor() {

    override fun process(document: org.asciidoctor.ast.Document, reader: PreprocessorReader) {
        val lines = reader.readLines()
        // Recognize a special value for the token. Forks can use this for actions to skip adding issues
        // by adding the token SDPI_API_ACCESS_TOKEN_SECRET in (Fork)->Settings->Secrets and variables->
        // Actions->Repository secrets. 
        if (githubToken.isNullOrEmpty() || githubToken == "skip") {
            logger.info { "Skip requesting issues, no Github token available" }
            reader.restoreLines(lines)
            return
        }

        val variables = lines
            .mapNotNull { VariableDeclaration.fromLine(it) }
            .filter { it.variableName == VAR_MILESTONE_PUBLICATION || it.variableName == VAR_MILESTONE_REVIEW }
            .associateBy(VariableDeclaration::variableName)

        val issues = try {
            IssueImport(
                githubToken,
                variables[VAR_MILESTONE_PUBLICATION]!!.variableValue,
                variables[VAR_MILESTONE_REVIEW]!!.variableValue
            ).requestIssues().issues()
        } catch (e: Exception) {
            logger.error(e) { e.message }
            throw e
        }
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

    private companion object : Logging {
        const val INSTR_OPEN_ISSUES = "// open issues are inserted here"
        const val INSTR_CLOSED_ISSUES = "// closed issues are inserted here"
        const val INSTR_TOI_ISSUES = "// toi issues are inserted here"

        const val VAR_MILESTONE_PUBLICATION = "sdpi_milestone_publication"
        const val VAR_MILESTONE_REVIEW = "sdpi_milestone_review"
    }
}