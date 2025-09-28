package org.sdpi.test.db

import org.sdpi.test.IheTestType
import org.sdpi.test.PeerType
import org.sdpi.test.Test

val preparatoryTestsTestFixture = Test.Fixture(
    id = "preparatory_test",
    name = "Preparatory Tests",
    tests = listOf(
        Test.Case(
            id = "certificate_deployment",
            name = "Certificate deployment",
            version = TEST_VERSION,
            shortDescription = "Verify working certificate infrastructure (deployment on devices)",
            peerType = PeerType.UNASSIGNED,
            iheTestType = IheTestType.PREPARATORY,
            description = "",
            evaluation = "",
            participants = listOf(SOMDS_CONSUMER, SOMDS_PROVIDER),
            steps = listOf(),
        ),
        Test.Case(
            id = "multicast_support",
            name = "Multicast support",
            version = TEST_VERSION,
            shortDescription = "Verify multicast support",
            peerType = PeerType.UNASSIGNED,
            iheTestType = IheTestType.PREPARATORY,
            description = "",
            evaluation = "",
            participants = listOf(SOMDS_CONSUMER, SOMDS_PROVIDER),
            steps = listOf(),
        ),
        Test.Case(
            id = "discovery_proxy",
            name = "Discovery proxy",
            version = TEST_VERSION,
            shortDescription = "Verify the discovery proxy to be running",
            peerType = PeerType.UNASSIGNED,
            iheTestType = IheTestType.PREPARATORY,
            description = "",
            evaluation = "",
            participants = listOf(DISCOVERY_PROXY),
            steps = listOf(),
        ),
        Test.Case(
            id = "network_connection",
            name = "Network connection",
            version = TEST_VERSION,
            shortDescription = "Verify the involved participants to be connected to the network",
            peerType = PeerType.UNASSIGNED,
            iheTestType = IheTestType.PREPARATORY,
            description = "",
            evaluation = "",
            participants = listOf(SOMDS_CONSUMER, SOMDS_PROVIDER, DISCOVERY_PROXY),
            steps = listOf(),
        ),
    )
)