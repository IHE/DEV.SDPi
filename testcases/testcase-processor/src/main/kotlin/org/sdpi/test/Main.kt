package org.sdpi.test

import kotlinx.serialization.json.Json
import org.sdpi.test.db.connectionEstablishmentTestFixture
import org.sdpi.test.db.discoveryTestFixture
import org.sdpi.test.db.preparatoryTestsTestFixture
import java.io.File

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val json = Json {
        prettyPrint = true
    }

    listOf(
        preparatoryTestsTestFixture,
        discoveryTestFixture,
        connectionEstablishmentTestFixture,
    ).forEach {
        File("${it.id}.json").writeText(
            json.encodeToString(it)
        )
    }
}