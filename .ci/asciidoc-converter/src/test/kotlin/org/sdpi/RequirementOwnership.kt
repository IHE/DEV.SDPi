package org.sdpi

import org.junit.jupiter.api.Test
import org.sdpi.asciidoc.model.OwningContext
import org.sdpi.asciidoc.model.SdpiRequirement2
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RequirementOwnership {

    @Test
    fun verifyRequirementOwnership() {
        val engine = TestRunner("req-ownership")
        val processor = engine.performTest()

        val req = processor.infoCollector.requirements()

        checkOwner(req[100], OwningContext.PROFILE, "Profile-A")
        checkOwner(req[200], OwningContext.PROFILE_OPTION, "OptionA")
        checkOwner(req[300], OwningContext.USE_CASE, "stad")
        checkOwner(req[400], OwningContext.CONTENT_MODULE, "biceps")
        checkOwner(req[500], OwningContext.GATEWAY, "stargate")
        checkOwner(req[600], OwningContext.PROTOCOL, "pigeon")

    }

    private fun checkOwner(req: SdpiRequirement2?, type: OwningContext, strId: String) {
        assertNotNull(req)
        assertNotNull(req.owner)
        assertEquals(type, req.owner!!.type)
        assertEquals(strId, req.owner!!.id)
    }
}