package org.sdpi

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.kotlin.logger
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.sdpi.asciidoc.extension.*
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Options to configure AsciidocConverter.
 */
class ConverterOptions(
    /**
     * Token to access the GitHub api. For including issues
     * in the document output, for example.
     */
    val githubToken: String? = null,

    /**
     * Defines the target format for the document output. See
     * https://docs.asciidoctor.org/asciidoctorj/latest/asciidoctor-api-options/#backend
     * Typically "html" or "pdf"
     */
    val outputFormat: String = "html",

    /**
     * When true, document structure is written to the log stream
     * for diagnostics.
     */
    val dumpStructure: Boolean = false,

    /**
     * When true, the document output is simplified for unit
     * tests. For example, the style sheet is not included.
     */
    val generateTestOutput: Boolean = false,

    /**
     * Folder where extracts (requirements, use-cases, etc.) should
     * be placed. If null, the extracts won't be written.
     */
    val extractsFolder: Path? = null,
) {
    companion object {
        private const val DEFAULT_EXTRACTS_FOLDER: String = "referenced-artifacts"

        fun makeDefaultPath(strOutputFolder: String): Path {
            return Path.of(strOutputFolder, DEFAULT_EXTRACTS_FOLDER)
        }
    }
}


class AsciidocConverter(
    private val inputType: Input,
    private val outputFile: OutputStream,
    private val conversionOptions: ConverterOptions,
) : Runnable {

    val anchorCollector = DocumentAnchorCollector()

    fun documentAnchors() = anchorCollector.getKnownAnchors()

    override fun run() {
        val options = Options.builder()
            .safe(SafeMode.UNSAFE)
            .backend(conversionOptions.outputFormat)
            .sourcemap(true)
            .headerFooter(!conversionOptions.generateTestOutput)
            .toStream(outputFile).build()
        val bEnablePrePostProcessing = true

        val asciidoctor = Asciidoctor.Factory.create()

        val anchorReplacements = AnchorReplacementsMap()

        val transactionActorsProcessor = TransactionActorsProcessor()

        // Formats sdpi_requirement blocks & their content.
        //   * RequirementBlockProcessor2 handles the containing sdpi_requirement block
        //   * RelatedBlockProcessor handles [RELATED] blocks within requirement blocks.
        //   * RequirementExampleBlockProcessor handles [EXAMPLE] blocks within requirement blocks.
        //   * TransactionActorsProcessor handles definitions of actor contributions within
        //     transaction sections.
        asciidoctor.javaExtensionRegistry().block(RequirementBlockProcessor2())
        asciidoctor.javaExtensionRegistry().block(RelatedBlockProcessor())
        asciidoctor.javaExtensionRegistry().block(RequirementExampleBlockProcessor())
        asciidoctor.javaExtensionRegistry().block(transactionActorsProcessor)

        asciidoctor.javaExtensionRegistry().treeprocessor(NumberingProcessor(null, anchorReplacements))

        // Gather bibliography entries.
        val bibliographyCollector = BibliographyCollector()
        asciidoctor.javaExtensionRegistry().treeprocessor(bibliographyCollector)

        // Gather profiles, the transactions, use cases they include.
        val profileTransactionCollector = TransactionIncludeProcessor()
        asciidoctor.javaExtensionRegistry().blockMacro(profileTransactionCollector)

        val profileUseCaseCollector = UseCaseIncludeProcessor()
        asciidoctor.javaExtensionRegistry().blockMacro(profileUseCaseCollector)

        val profileContentModuleCollector = ContentModuleIncludeProcessor()
        asciidoctor.javaExtensionRegistry().blockMacro(profileContentModuleCollector)

        // Gather SDPI specific information from the document such as
        // requirements and use-cases.
        val infoCollector = SdpiInformationCollector(
            bibliographyCollector,
            transactionActorsProcessor,
            profileTransactionCollector,
            profileUseCaseCollector,
            profileContentModuleCollector
        )
        asciidoctor.javaExtensionRegistry().treeprocessor(infoCollector)

        // Gather anchors in the document so we can verify there aren't any invalid links.
        asciidoctor.javaExtensionRegistry().treeprocessor(anchorCollector)

        // Support to insert tables of requirements etc. sdpi_requirement_table macros.
        // Block macro processors insert placeholders that are populated when the tree is ready.
        // Tree processors fill in the placeholders.
        asciidoctor.javaExtensionRegistry().blockMacro(AddRequirementQueryPlaceholder())
        asciidoctor.javaExtensionRegistry().blockMacro(AddICSPlaceholder())
        asciidoctor.javaExtensionRegistry().blockMacro(AddTransactionQueryPlaceholder())
        asciidoctor.javaExtensionRegistry().blockMacro(AddContentModuleQueryPlaceholder())

        asciidoctor.javaExtensionRegistry().treeprocessor(PopulateTables(infoCollector))

        // Handle inline macros to cross-reference information from the document tree.
        asciidoctor.javaExtensionRegistry().inlineMacro(RequirementReferenceMacroProcessor(infoCollector))
        asciidoctor.javaExtensionRegistry().inlineMacro(UseCaseReferenceMacroProcessor(infoCollector))
        asciidoctor.javaExtensionRegistry().inlineMacro(ActorReferenceMacroProcessor(infoCollector))
        asciidoctor.javaExtensionRegistry().inlineMacro(ContentModuleReferenceMacroProcessor(infoCollector))
        asciidoctor.javaExtensionRegistry().inlineMacro(TransactionReferenceMacroProcessor(infoCollector))
        asciidoctor.javaExtensionRegistry().inlineMacro(ProfileReferenceMacroProcessor(infoCollector))

        if (bEnablePrePostProcessing) {
            asciidoctor.javaExtensionRegistry().preprocessor(IssuesSectionPreprocessor(conversionOptions.githubToken))
            asciidoctor.javaExtensionRegistry().preprocessor(DisableSectNumsProcessor())
        }

        if (bEnablePrePostProcessing) {
            println("Enable pre post processing.")
            val referenceSanitizerPre = ReferenceSanitizerPreprocessor(anchorReplacements)
            asciidoctor.javaExtensionRegistry().preprocessor(referenceSanitizerPre)
            if (conversionOptions.outputFormat == "html") {
                // Post processor not supported for PDFs
                // https://docs.asciidoctor.org/asciidoctorj/latest/extensions/postprocessor/
                asciidoctor.javaExtensionRegistry().postprocessor(ReferenceSanitizerPostprocessor(anchorReplacements))
            }
        }

        // Dumps tree of document structure to stdio.
        // Best not to use for very large documents!
        // Note: enabling this breaks variable replacement for {var_transaction_id}. Unclear why.
        if (conversionOptions.dumpStructure) {
            asciidoctor.javaExtensionRegistry().treeprocessor(DumpTreeInfo())
        }

        asciidoctor.requireLibrary("asciidoctor-diagram") // enables plantuml

        when (inputType) {
            is Input.FileInput -> asciidoctor.convertFile(inputType.file, options)
            is Input.StringInput -> asciidoctor.convert(inputType.string, options)
        }

        //profileTransactionCollector.dump()
        //anchorReplacements.dump()
        //anchorCollector.dumpKnownAnchors()

        for (reqOwner in infoCollector.requirementOwners()) {
            print("R${reqOwner.key}")
            reqOwner.value?.dump()
            println()
        }

        if (conversionOptions.extractsFolder != null) {
            val jsonFormatter = Json {
                prettyPrint = true
                explicitNulls = false
            }

            writeArtifact(
                "sdpi-profiles",
                jsonFormatter.encodeToString(infoCollector.profiles())
            )
            writeArtifact(
                "sdpi-use-cases",
                jsonFormatter.encodeToString(infoCollector.useCases())
            )
            writeArtifact(
                "sdpi-content-modules",
                jsonFormatter.encodeToString(infoCollector.contentModules().values)
            )
            writeArtifact(
                "sdpi-transactions",
                jsonFormatter.encodeToString(infoCollector.transactions().values)
            )
            writeArtifact(
                "sdpi-requirements",
                jsonFormatter.encodeToString(infoCollector.requirements().values)
            )
        }

        asciidoctor.shutdown()

    }

    private fun writeArtifact(strArtifactName: String, strArtifact: String) {
        if (conversionOptions.extractsFolder != null) {
            Files.createDirectories(conversionOptions.extractsFolder)
            Path.of(conversionOptions.extractsFolder.absolutePathString(), "${strArtifactName}.json").toFile()
                .writeText(strArtifact)
        }
    }

    sealed interface Input {
        data class FileInput(val file: File) : Input
        data class StringInput(val string: String) : Input
    }
}