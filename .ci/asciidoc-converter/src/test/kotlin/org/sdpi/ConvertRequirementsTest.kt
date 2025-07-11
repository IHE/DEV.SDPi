package org.sdpi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files

/**
 * Tests html output generation for semantic requirements in
 * an AsciiDoc source file.
 */
internal class ConvertRequirementsTest {

    /**
     * Minimal technical requirement.
     */
    @Test
    fun testMinimumRequirement() {
        performTest("min_requirement")
    }

    /**
     * Complete technical requirement including normative, note,
     * example and related sections.
     */
    @Test
    fun testFullRequirement() {
        performTest("full_requirement")
    }

    /**
     * Requirement that references another standard.
     */
    @Test
    fun testRefIcsRequirement() {
        performTest("ref_ics_requirement")
    }

    /**
     * Risk mitigation requirement.
     */
    @Test
    fun testRiskRequirement() {
        performTest("risk_requirement")
    }

    /**
     * Requirement within a use case; the use-case id is discovered automatically.
     */
    @Test
    fun testUseCaseRequirement() {
        performTest("use_case_requirement")
    }

    /**
     * No test for this one because I can't figure out what it is for.
     */
    fun testIheRequirement() {

    }

    /**
     * Source includes a reference to the requirement before and after
     * the requirement definition.
     */
    @Test
    fun testRequirementRef() {
        performTest("cross_ref_requirement")
    }

    private fun performTest(strTestFile: String) {
        val engine = TestRunner(strTestFile)
        engine.performTest()
    }
}