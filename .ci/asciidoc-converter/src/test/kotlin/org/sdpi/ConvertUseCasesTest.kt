package org.sdpi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files

/**
 * Tests html output generation for semantic use cases
 * specified in an AsciiDoc file.
 */
internal class ConvertUseCasesTest {

    @Test
    fun useCaseWithCrossRef() {
        performTest("use_case")
    }

    private fun performTest(strTestFile: String) {
        val engine = TestRunner(strTestFile)
        engine.performTest()
    }
}