package org.sdpi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.file.Files

internal class ConvertAndVerifySupplementTest {
    @Test
    fun testOffset() {
        val testInputResourceName = "test_offset_input.adoc"
        val expectedStructureResourceName = "test_offset_expected_structure.txt"
        performTest(testInputResourceName, expectedStructureResourceName)
    }

    @Test
    fun testLevel() {
        val testInputResourceName = "test_level_input.adoc"
        val expectedStructureResourceName = "test_level_expected_structure.txt"
        performTest(testInputResourceName, expectedStructureResourceName)
    }

    private fun performTest(testInputResourceName: String, expectedStructureResourceName: String) {
        val expectedOutput =
            javaClass.classLoader.getResourceAsStream(expectedStructureResourceName)?.reader()?.readText()
                ?: throw Exception("Read failed")
        val actualOutput = ByteArrayOutputStream(expectedOutput.toByteArray().size)

        Files.createTempFile("asciidoc-converter-test", ".tmp").toFile().also {
            AsciidocConverter(
                AsciidocConverter.Input.StringInput(
                    javaClass.classLoader.getResourceAsStream(testInputResourceName)?.reader()?.readText()
                        ?: throw Exception("Read failed")
                ),
                it, null,
                AsciidocConverter.Mode.Test(actualOutput)
            ).run()
        }.also {
            it.delete()
        }

        assertEquals(expectedOutput, actualOutput.toString(Charsets.UTF_8))
    }
}