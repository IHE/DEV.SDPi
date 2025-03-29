package org.sdpi

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.sdpi.asciidoc.extension.AnchorReplacementsMap
import org.sdpi.asciidoc.extension.NumberingProcessor
import java.io.ByteArrayOutputStream

/**
 * Check that block levels are correctly renumbered by the numbering tree
 * processor and that the expected html output is generated. This is so
 * content authors can indicate where document from the supplement will
 * be inserted into the Technical Framework document.
 */
internal class BlockNumberingTest {
    /**
     * Check heading numbers are correctly applied, following heading offsets
     * specified in the input.
     */
    @Test
    fun testOffset() {
        val testInputResourceName = "test_offset_input.adoc"
        val expectedStructureResourceName = "test_offset_expected_structure.txt"
        runNumberingProcessorTest(testInputResourceName, expectedStructureResourceName)
    }

    /**
     * Test html output generation from an AsciiDoc source file that includes
     * heading offsets that need to be applied to heading numbering.
     */
    @Test
    fun testOffsetDocGeneration() {
        runDocumentGenerationTest("test_offset_input")
    }

    /**
     * Check heading numbers are correctly applied, following relative levels
     * specified in the input.
     */
    @Test
    fun testLevels() {
        val testInputResourceName = "test_level_input.adoc"
        val expectedStructureResourceName = "test_level_expected_structure.txt"
        runNumberingProcessorTest(testInputResourceName, expectedStructureResourceName)
    }

    /**
     * Test html output generation from an AsciiDoc source file that includes
     * heading levels that need to be applied to heading numbering.
     */
    @Test
    fun testLevelsDocGeneration() {
        runDocumentGenerationTest("test_level_input")
    }

    private fun runNumberingProcessorTest(testInputResourceName: String, expectedStructureResourceName: String) {
        val input = readResource(testInputResourceName)
        val expectedOutput = readResource(expectedStructureResourceName)
        val actualOutput = ByteArrayOutputStream(expectedOutput.toByteArray().size)

        val options = Options.builder()
            .safe(SafeMode.UNSAFE)
            .backend("html")
            .build()

        val asciidoctor = Asciidoctor.Factory.create()

        val anchorReplacements = AnchorReplacementsMap()
        asciidoctor.javaExtensionRegistry().treeprocessor(
            NumberingProcessor(actualOutput, anchorReplacements),
        )

        asciidoctor.convert(input, options)

        // Windows and Linux use different line endings but the output uses
        // \r line endings on Windows. Resolve this by normalizing both strings.
        val normalizedExpected = expectedOutput.lines().joinToString(System.lineSeparator())
        val normalizedOutput = actualOutput.toString(Charsets.UTF_8).lines().joinToString(System.lineSeparator())
        assertEquals(normalizedExpected, normalizedOutput)
    }

    private fun readResource(strResourceName: String): String {
        return javaClass.classLoader.getResourceAsStream(strResourceName)?.reader()?.readText()
            ?: throw Exception("Read failed")
    }

    private fun runDocumentGenerationTest(strTestFile: String) {
        val engine = TestRunner(strTestFile)
        engine.performTest()
    }
}