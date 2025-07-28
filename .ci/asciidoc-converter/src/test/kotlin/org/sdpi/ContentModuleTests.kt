package org.sdpi

import org.junit.jupiter.api.Test
import org.sdpi.asciidoc.model.Obligation
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ContentModuleTests {

    /*
        Check we find content modules and profile reference to them.
        - single profile
        - two content modules
        - both content modules referenced from profile.
     */
    @Test
    fun gatherProfileContentModule() {
        val engine = TestRunner("content-module")
        val processor = engine.performTest()

        // Check modules were parsed
        val modules = processor.infoCollector.contentModules()
        assertEquals(2, modules.size)

        val module1 = modules["biceps"]
        assertNotNull(module1)
        assert(module1.id == "biceps")

        val module2 = modules["pump"]
        assertNotNull(module2)
        assert(module2.id == "pump")

        // Check profile includes the modules.
        val profile = processor.infoCollector.getProfile("Profile-A")
        assertNotNull(profile)

        val refs = profile.contentModuleReferences
        assertNotNull(refs)
        assertEquals(4, refs.size)

        assertNotNull(refs.find { it.contentModuleId == "biceps" && it.actorId == "content-consumer" && it.obligation == Obligation.REQUIRED })
        assertNotNull(refs.find { it.contentModuleId == "biceps" && it.actorId == "content-creator" && it.obligation == Obligation.REQUIRED })

        assertNotNull(refs.find { it.contentModuleId == "pump" && it.actorId == "content-consumer" && it.obligation == Obligation.OPTIONAL })
        assertNotNull(refs.find { it.contentModuleId == "pump" && it.actorId == "content-creator" && it.obligation == Obligation.OPTIONAL })
    }

    /*
        Check we find content modules and profile reference to them.
        - single profile with an option
        - two content modules
        - one content module referenced from profile.
        - different content module referenced from profile option.
    */
    @Test
    fun gatherProfileAndOptionTransactions() {
        val engine = TestRunner("content-module-option")
        val processor = engine.performTest()

        // Check profile includes one module reference for both actors...
        val profile = processor.infoCollector.getProfile("Profile-A")
        assertNotNull(profile)

        val refs = profile.contentModuleReferences
        assertNotNull(refs)
        assertEquals(2, refs.size)

        assertNotNull(refs.find { it.contentModuleId == "biceps" && it.actorId == "content-consumer" && it.obligation == Obligation.REQUIRED })
        assertNotNull(refs.find { it.contentModuleId == "biceps" && it.actorId == "content-creator" && it.obligation == Obligation.REQUIRED })

        // Check profile option references other module for both actors...
        val optionRefs = profile.findOption("OptionA")?.contentModuleReferences
        assertNotNull(optionRefs)

        assertNotNull(optionRefs.find { it.contentModuleId == "pump" && it.actorId == "content-consumer" && it.obligation == Obligation.OPTIONAL })
        assertNotNull(optionRefs.find { it.contentModuleId == "pump" && it.actorId == "content-creator" && it.obligation == Obligation.OPTIONAL })

    }
}