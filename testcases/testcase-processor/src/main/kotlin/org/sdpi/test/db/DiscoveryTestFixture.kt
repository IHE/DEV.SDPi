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
            shortDescription = "A SOMDS Consumer detects a SOMDS Provider based on implicit and/or explicit discovery",
            peerType = PeerType.PEER_TO_PEER,
            preconditions = """
                - The SOMDS Consumer or the SOMPDS Provider are up and running.
                - The SOMDS Consumer is able to signify discovered SOMDS Providers.
                - The SDC network supports UDP multicast.
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
                        further action if a discovery scope match is met.
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

val connectionEstablishment = Test.Fixture(
    id = "sdpi_connection_establishment",
    name = "SDPi connection establishment",
    tests = listOf(
        Test.Case(
            id = "connection_establishment",
            name = "Connection establishment",
            version = TEST_VERSION,
            shortDescription =
                """
                    A SOMDS Consumer connects to SOMDS Provider by retrieving the SOMDS Provider's service description
                    and MDIB as well as by subscribing to the SOMDS Provider's MDIB reports.
                """.trimIndent(),
            peerType = PeerType.PEER_TO_PEER,
            preconditions = """
                - The SOMDS Consumer has discovered the SOMDS Provider and is in possession of the SOMDS Provider's
                  physical endpoint address.
                - The SOMDS Consumer can signify the connection status to the user.
            """.trimIndent(),
            description =
                """
                    Instruct the SOMDS Consumer to connect to the SOMDS Provider.
                """.trimIndent(),
            evaluation =
                """
                    Wait for the SOMDS Consumer to signify the connection establishment.
                """.trimIndent(),
            participants = listOf(
                SOMDS_CONSUMER,
                SOMDS_PROVIDER,
            ),
            steps = listOf(
                TestStep(
                    id = "1",
                    initiator = SOMDS_CONSUMER,
                    responder = SOMDS_PROVIDER,
                    transaction = "DEV-25",
                    messageType = MessageType.SOAP,
                    support = Support.REQUIRED,
                    security = Security.TLS,
                    description = """
                        The SOMDS Consumer requests the BICEPS services that are supported by the SOMDS Provider.
                    """.trimIndent()
                ),
                TestStep(
                    id = "2",
                    initiator = SOMDS_CONSUMER,
                    responder = SOMDS_PROVIDER,
                    transaction = "DEV-27",
                    messageType = MessageType.SOAP,
                    support = Support.OPTIONAL,
                    security = Security.TLS,
                    description = """
                        The SOMDS Consumer subscribes to MDIB reports it is interested in of the SOMDS Provider.
                    """.trimIndent()
                ),
                TestStep(
                    id = "3",
                    initiator = SOMDS_CONSUMER,
                    responder = SOMDS_PROVIDER,
                    transaction = "DEV-30",
                    messageType = MessageType.SOAP,
                    support = Support.REQUIRED,
                    security = Security.TLS,
                    description = """
                        The SOMDS Consumer requests the MDIB of the SOMDS Provider for initial MDIB snapshot retrieval.
                    """.trimIndent()
                ),
                TestStep(
                    id = "4",
                    initiator = SOMDS_PROVIDER,
                    responder = SOMDS_CONSUMER,
                    transaction = "DEV-28",
                    messageType = MessageType.SOAP,
                    support = Support.OPTIONAL,
                    security = Security.TLS,
                    description = """
                        If the SOMDS Consumer subscribed to the SOMDS Provider, the SOMDS Provider publishes MDIB
                        reports to the SOMDS Consumer.
                    """.trimIndent()
                ),
            )
        )
    )
)