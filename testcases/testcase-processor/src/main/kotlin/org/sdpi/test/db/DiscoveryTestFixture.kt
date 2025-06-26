package org.sdpi.test.db

import org.sdpi.test.Actor
import org.sdpi.test.MessageType
import org.sdpi.test.PeerType
import org.sdpi.test.ProfileOption
import org.sdpi.test.Security
import org.sdpi.test.Support
import org.sdpi.test.Test
import org.sdpi.test.TestParticpant
import org.sdpi.test.TestStep

val TEST_VERSION = "1.0.0"

val SOMDS_PROVIDER = TestParticpant(
    actor = Actor.SOMDS_PROVIDER
)

val SOMDS_CONSUMER = TestParticpant(
    actor = Actor.SOMDS_CONSUMER
)

val SOMDS_PROVIDER_WITH_DISCOVERY_PROXY = TestParticpant(
    actor = Actor.SOMDS_PROVIDER,
    profileOptions = listOf(ProfileOption.DISCOVERY_PROXY),
)

val SOMDS_CONSUMER_WITH_DISCOVERY_PROXY = TestParticpant(
    actor = Actor.SOMDS_CONSUMER,
    profileOptions = listOf(ProfileOption.DISCOVERY_PROXY),
)

val DISCOVERY_PROXY = TestParticpant(
    actor = Actor.DISCOVERY_PROXY,
)

val discoveryTestFixture = Test.Fixture(
    id = "sdpi_discovery",
    name = "SDPi discovery",
    tests = listOf(
        Test.Case(
            id = "adhoc_discovery",
            name = "Adhoc Discovery",
            version = TEST_VERSION,
            shortDescription = "A SOMDS Consumer detects a SOMDS Provider based on SDPi discovery",
            peerType = PeerType.PEER_TO_PEER,
            preconditions = """
                - The SOMDS Consumer and the SOMDS Provider are ready to be operated.
                - The SOMDS Consumer is able to signify discovered SOMDS Providers.
                - The SDC network supports UDP multicast.
            """.trimIndent(),
            description =
                """
                    Configure the SOMDS Consumer and SOMDS Provider in a way that there will be a discovery scope match.
                """.trimIndent(),
            evaluation =
                """
                    Once the SOMDS Provider and SOMDS Consumer are running, wait for the SOMDS Consumer to indicate the
                    presence of the SOMDS Provider. The discovery process has successfully finished.
                """.trimIndent(),
            participants = listOf(
                SOMDS_CONSUMER,
                SOMDS_PROVIDER
            ),
            steps = listOf(
                TestStep(
                    id = "1",
                    initiator = SOMDS_PROVIDER,
                    responder = null,
                    transaction = "DEV-23",
                    messageType = MessageType.SOAP,
                    support = Support.OPTIONAL,
                    security = Security.UNSECURED,
                    description = """
                        The SOMDS Provider sends a hello message, which is processed by the SOMDS Consumer to take
                        further action if a discovery types and scopes match is met.
                    """.trimIndent()
                ),
                TestStep(
                    id = "2",
                    initiator = SOMDS_CONSUMER,
                    responder = SOMDS_PROVIDER,
                    transaction = "DEV-24",
                    messageType = MessageType.SOAP,
                    support = Support.OPTIONAL,
                    security = Security.UNSECURED,
                    description = """
                        The SOMDS Consumer sends a probe message, which is processed by the SOMDS Provider to answer
                        with a probe match message if types and scopes match.
                    """.trimIndent()
                ),
            )
        ),
        Test.Case(
            id = "managed_discovery",
            name = "Managed Discovery",
            version = TEST_VERSION,
            shortDescription =
                """
                    A SOMDS Consumer detects a SOMDS Provider based on implicit and/or explicit discovery by using a
                    Discovery Proxy.
                """.trimIndent(),
            peerType = PeerType.PEER_TO_PEER,
            preconditions = """
                - The Discovery Proxy is up and running.
                - The SOMDS Consumer is configured to be used with the Discovery Proxy.
                - The SOMDS Provider is configured to be used with the Discovery Proxy.
            """.trimIndent(),
            description =
                """
                    Turn on the SOMDS Consumer and SOMDS Provider in any order.
                    
                    Configure the SOMDS Consumer and SOMDS Provider in a way that there will be a discovery scope match.
                """.trimIndent(),
            evaluation =
                """
                    Once the SOMDS Provider and SOMDS Consumer are running, wait for the SOMDS Consumer to indicate the
                    presence of the SOMDS Provider. The discovery process has successfully finished.
                """.trimIndent(),
            participants = listOf(
                SOMDS_CONSUMER_WITH_DISCOVERY_PROXY,
                SOMDS_PROVIDER_WITH_DISCOVERY_PROXY,
                DISCOVERY_PROXY
            ),
            steps = listOf(
                TestStep(
                    id = "1",
                    initiator = SOMDS_PROVIDER_WITH_DISCOVERY_PROXY,
                    responder = null,
                    transaction = "DEV-46",
                    messageType = MessageType.SOAP,
                    support = Support.OPTIONAL,
                    security = Security.TLS,
                    description = """
                        The SOMDS Provider sends a hello message, which is processed by the Discovery Proxy.
                        
                        The Discovery Proxy forwards the hello message to the SOMDS Consumer.
                    """.trimIndent()
                ),
                TestStep(
                    id = "2",
                    initiator = SOMDS_CONSUMER_WITH_DISCOVERY_PROXY,
                    responder = DISCOVERY_PROXY,
                    transaction = "DEV-47",
                    messageType = MessageType.SOAP,
                    support = Support.OPTIONAL,
                    security = Security.TLS,
                    description = """
                        The SOMDS Consumer sends a probe message, which is answered by the Discovery Proxy with a probe
                        match message if types and scopes match. 
                    """.trimIndent()
                ),
            )
        )
    ),
)

