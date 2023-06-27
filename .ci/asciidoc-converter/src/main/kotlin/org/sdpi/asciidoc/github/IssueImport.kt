package org.sdpi.asciidoc.github

import org.apache.logging.log4j.kotlin.Logging
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHubBuilder

data class Issues(
    val toiIssues: List<GHIssue>,
    val closedIssuesInMilestones: List<GHIssue>,
    val openIssuesInMilestones: List<GHIssue>
)

class IssueImport(
    private val token: String,
    private val publicationMilestone: String,
    private val reviewMilestone: String
) {
    private val toiIssues: MutableList<GHIssue> = mutableListOf()
    private val closedIssuesInMilestones: MutableList<GHIssue> = mutableListOf()
    private val openIssuesInMilestones: MutableList<GHIssue> = mutableListOf()

    private fun issuesInMilestones(
        repository: GHRepository,
        state: GHIssueState,
        vararg milestones: String
    ): List<GHIssue> {
        return milestones.map { ms ->
            repository.listMilestones(GHIssueState.ALL).first { it.title == ms }.let {
                repository.getIssues(state, it)
            }
        }.flatten()
    }

    private companion object : Logging {
        const val IHE_ORG = "IHE"
        const val SDPI_REPO = "DEV.SDPi"
        const val TOI_LABEL = "Topic of Interest"
    }

    fun issues() = Issues(
        toiIssues,
        closedIssuesInMilestones,
        openIssuesInMilestones
    )

    fun requestIssues(): IssueImport {
        logger.info { "Request open, closed and TOI issues" }

        val github = GitHubBuilder().withOAuthToken(token).build()

        val iheOrganization = try {
            github.getOrganization(IHE_ORG).also {
                require(it != null) {
                    "Could not find/connect to IHE/DEV.SDPi"
                }
            }
        } catch (e: Exception) {
            throw Exception("Could not connect to IHE organization: ${e.message}", e)
        }

        val sdpiRepository = iheOrganization.repositories[SDPI_REPO]
        require(sdpiRepository != null) {
            "Could not find/connect to IHE/DEV.SDPi"
        }

        openIssuesInMilestones.addAll(
            issuesInMilestones(sdpiRepository, GHIssueState.OPEN, publicationMilestone, reviewMilestone)
        )

        closedIssuesInMilestones.addAll(
            issuesInMilestones(sdpiRepository, GHIssueState.CLOSED, publicationMilestone, reviewMilestone)
        )

        toiIssues.addAll(
            sdpiRepository.queryIssues().label(TOI_LABEL).state(GHIssueState.OPEN).list().toList()
        )

        logger.info { "Found ${openIssuesInMilestones.size} open, ${closedIssuesInMilestones.size} closed and ${toiIssues.size} TOI issues" }

        return this
    }
}