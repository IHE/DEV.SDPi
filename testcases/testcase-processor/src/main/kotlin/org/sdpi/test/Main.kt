package org.sdpi.test

import kotlinx.serialization.json.Json
import org.sdpi.test.db.connectionEstablishmentTestFixture
import org.sdpi.test.db.discoveryTestFixture
import org.sdpi.test.db.preparatoryTestsTestFixture
import java.io.File

fun main() {
    val json = Json {
        prettyPrint = true
    }

    listOf(
        preparatoryTestsTestFixture,
        discoveryTestFixture,
        connectionEstablishmentTestFixture,
    ).forEach {
        File("testcases/resources/${it.id}.json").writeText(
            json.encodeToString(it)
        )
    }
}