package org.sdpi

import org.junit.jupiter.api.Test

/**
 * Test generation of ics tables of requirements
 */
class IcsTableGenerationTests {

    @Test
    fun tableNoFilter() {
        performTest("ics_no_filter")
    }

    @Test
    fun tableFiltered() {
        performTest("ics_filtered")
    }

    private fun performTest(strTestFile: String) {
        val engine = TestRunner(strTestFile)
        engine.performTest()
    }
}