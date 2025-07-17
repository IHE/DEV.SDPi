package org.sdpi


import org.sdpi.asciidoc.model.GherkinStepType
import org.sdpi.asciidoc.model.Obligation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests html output generation for semantic use cases
 * specified in an AsciiDoc file.
 */
internal class ConvertUseCasesTest {

    /*
        Check we find use case and a profile's relationship to them.
            - single profile
            - one use case, two scenarios.
            - use case applies to both consumer and provider actors.
            - use case referenced from profile.
    */
    @Test
    fun useCaseWithCrossRef() {
        val engine = TestRunner("use_case")
        val processor = engine.performTest()

        val useCases = processor.infoCollector.useCases()
        assertEquals(1, useCases.size)

        // Check use case parsing
        val stad = useCases["stad"]
        assertNotNull(stad)
        assertEquals("Synchronized time across devices", stad.title)
        assertEquals("stad", stad.id)

        val backSteps = stad.specification.background
        assertEquals(2, backSteps.size)
        assertEquals(GherkinStepType.GIVEN, backSteps[0].step)
        assertEquals(GherkinStepType.AND, backSteps[1].step)

        val scenarios = stad.specification.scenarios
        assertEquals(2, scenarios.size)

        val scenario1 = scenarios[0]
        assertEquals(3, scenario1.specification.size)
        assertEquals(GherkinStepType.GIVEN, scenario1.specification[0].step)
        assertEquals(GherkinStepType.WHEN, scenario1.specification[1].step)
        assertEquals(GherkinStepType.THEN, scenario1.specification[2].step)

        // Check reference from profile.
        val profile = processor.infoCollector.getProfile("Profile-A")
        assertNotNull(profile)

        val refs = profile.useCaseReferences
        assertNotNull(refs)
        assertEquals(2, refs.size)

        assertNotNull(refs.find { it.actorId == "somds-consumer" && it.obligation == Obligation.OPTIONAL })
        assertNotNull(refs.find { it.actorId == "somds-provider" && it.obligation == Obligation.REQUIRED })
    }

    /*
     Check we find use case and a profile's relationship to them.
         - single profile with option
         - one use case, two scenarios.
         - use case applies to both consumer and provider actors.
         - use case referenced from profile option
 */
    @Test
    fun useCaseInProfileOption() {
        val engine = TestRunner("use_case_option")
        val processor = engine.performTest()

        val useCases = processor.infoCollector.useCases()
        assertEquals(1, useCases.size)

        // Check reference from profile (none)
        val profile = processor.infoCollector.getProfile("Profile-A")
        assertNotNull(profile)

        val refs = profile.useCaseReferences
        assertNotNull(refs)
        assertEquals(0, refs.size)

        // check reference from profile option
        val option = profile.findOption("OptionA")
        assertNotNull(option)

        val optionRefs = option.useCaseReferences
        assertNotNull(optionRefs)
        assertEquals(2, optionRefs.size)

        assertNotNull(optionRefs.find { it.actorId == "somds-consumer" && it.obligation == Obligation.OPTIONAL })
        assertNotNull(optionRefs.find { it.actorId == "somds-provider" && it.obligation == Obligation.REQUIRED })
    }
}