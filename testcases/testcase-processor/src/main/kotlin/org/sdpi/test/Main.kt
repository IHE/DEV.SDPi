package org.sdpi.test

import kotlinx.serialization.json.Json
import org.sdpi.test.db.connectionEstablishment
import org.sdpi.test.db.discoveryTestFixture
import java.io.File

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val json = Json {
        prettyPrint = true
    }
    File("${discoveryTestFixture.id}.json").writeText(
        json.encodeToString(discoveryTestFixture)
    )
    File("${connectionEstablishment.id}.json").writeText(
        json.encodeToString(connectionEstablishment)
    )
}