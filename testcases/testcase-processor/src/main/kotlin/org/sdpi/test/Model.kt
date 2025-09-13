package org.sdpi.test

import kotlinx.serialization.Serializable

@Serializable
data class TestDataBase(
    val testFixtures: List<Test>,
)

@Serializable
sealed interface Test {
    @Serializable
    data class Fixture(
        val id: String,
        val name: String,
        val tests: List<Test>,
    ) : Test

    @Serializable
    data class Sequence(
        val id: String,
        val name: String,
        val tests: List<Test>,
    )

    @Serializable
    data class Case(
        val id: String,
        val name: String,
        val version: String,
        val shortDescription: String,
        val peerType: PeerType,
        val iheTestType: IheTestType = IheTestType.CONNECTATHON,
        val preconditions: String = "",
        val specificInstructions: String = "",
        val description: String,
        val evaluation: String,
        val participants: List<TestParticpant>,
        val steps: List<TestStep>,
    ) : Test
}


@Serializable
enum class PeerType {
    UNASSIGNED,
    NO_PEER,
    PEER_TO_PEER,
    GROUP;
}

@Serializable
enum class IheTestType {
    CONNECTATHON,
    PREPARATORY
}

@Serializable
data class TestParticpant(
    val actor: Actor,
    val profileOptions: List<ProfileOption> = listOf(),
)

@Serializable
enum class Actor {
    SOMDS_PROVIDER,
    SOMDS_CONSUMER,
    DISCOVERY_PROXY
}

@Serializable
enum class ProfileOption {
    DISCOVERY_PROXY
}

@Serializable
data class TestStep(
    val id: String,
    val initiator: TestParticpant,
    val responder: TestParticpant? = null,
    val transaction: String,
    val messageType: MessageType,
    val support: Support,
    val security: Security,
    val description: String,
)

@Serializable
enum class MessageType {
    SOAP
}

@Serializable
enum class Support {
    OPTIONAL,
    REQUIRED
}

@Serializable
enum class Security {
    UNSECURED,
    TLS
}
