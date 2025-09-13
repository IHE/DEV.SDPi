package org.sdpi.test.db

import org.sdpi.test.MessageType
import org.sdpi.test.PeerType
import org.sdpi.test.Security
import org.sdpi.test.Support
import org.sdpi.test.Test
import org.sdpi.test.TestStep

val connectionEstablishmentTestFixture = Test.Fixture(
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