package org.sdpi

import kotlin.test.Test

internal class ReferenceVariables {

    /*
        If the processor reads the document title at the wrong time on
        an included file then variable substitution gets broken. Check
        that it isn't broken.
     */
    @Test
    fun checkVariables() {
        val engine = TestRunner("ref_variables")
        val processor = engine.performTest()
    }
}