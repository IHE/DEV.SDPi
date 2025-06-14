package org.sdpi

import org.junit.jupiter.api.Test
import org.sdpi.asciidoc.model.Contribution
import org.sdpi.asciidoc.model.Obligation
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransactionTests {

    /*
    Check we find transactions and a profile's relationship to them.
        - single profile
        - two transactions
        - both transactions referenced from profile.
     */
    @Test
    fun gatherProfileTransactions() {
        val engine = TestRunner("transactions")
        val processor = engine.performTest()
        val transactions = processor.infoCollector.transactions()

        // Decoded transactions
        assertEquals(2, transactions.size)
        assert(transactions.containsKey("DEV-1"))
        assert(transactions.containsKey("DEV-2"))

        // Profile references transactions.
        val profile = processor.infoCollector.getProfile("Profile-A")
        assertNotNull(profile)

        val refs = profile.transactionReferences
        assertNotNull(refs)

        val ref1 = refs.find { it.transactionId == "DEV-1" }
        assertNotNull(ref1)
        assertNotNull(ref1.obligations)
        assertNotNull(ref1.obligations.find { it.contribution == Contribution.INITIATOR && it.obligation == Obligation.REQUIRED })
        assertNotNull(ref1.obligations.find { it.contribution == Contribution.RECEIVER && it.obligation == Obligation.OPTIONAL })

        val ref2 = refs.find { it.transactionId == "DEV-2" }
        assertNotNull(ref2)
        assertNotNull(ref2.obligations)
        assertNotNull(ref2.obligations.find { it.contribution == Contribution.INITIATOR && it.obligation == Obligation.OPTIONAL })
        assertNotNull(ref2.obligations.find { it.contribution == Contribution.RESPONDER && it.obligation == Obligation.REQUIRED })
    }

    /*
   Check we find transactions and a profile's relationship to them.
       - single profile with an option
       - two transactions
       - both transactions referenced from profile.
       - transaction obligation different in profile and profile option.
    */
    @Test
    fun gatherProfileAndOptionTransactions() {
        val engine = TestRunner("transaction-option")
        val processor = engine.performTest()

        // Profile references transactions.
        val profile = processor.infoCollector.getProfile("Profile-A")
        assertNotNull(profile)

        val refs = profile.transactionReferences
        assertNotNull(refs)

        val ref1 = refs.find { it.transactionId == "DEV-1" }
        assertNotNull(ref1)
        assertNotNull(ref1.obligations)
        assertNotNull(ref1.obligations.find { it.contribution == Contribution.INITIATOR && it.obligation == Obligation.REQUIRED })
        assertNotNull(ref1.obligations.find { it.contribution == Contribution.RECEIVER && it.obligation == Obligation.OPTIONAL })

        val ref2 = refs.find { it.transactionId == "DEV-2" }
        assertNotNull(ref2)
        assertNotNull(ref2.obligations)
        assertNotNull(ref2.obligations.find { it.contribution == Contribution.INITIATOR && it.obligation == Obligation.OPTIONAL })
        assertNotNull(ref2.obligations.find { it.contribution == Contribution.RESPONDER && it.obligation == Obligation.REQUIRED })

        val option = profile.findOption("OptionA")
        assertNotNull(option)

        val refOption1 = option.transactionReferences.find { it.transactionId == "DEV-1" }
        assertNotNull(refOption1)
        assertNotNull(refOption1.obligations)
        assertNotNull(refOption1.obligations.find { it.contribution == Contribution.INITIATOR && it.obligation == Obligation.REQUIRED })
        assertNotNull(refOption1.obligations.find { it.contribution == Contribution.RECEIVER && it.obligation == Obligation.REQUIRED })

        val refOption2 = option.transactionReferences.find { it.transactionId == "DEV-2" }
        assertNotNull(refOption2)
        assertNotNull(refOption2.obligations)
        assertNotNull(refOption2.obligations.find { it.contribution == Contribution.INITIATOR && it.obligation == Obligation.REQUIRED })
        assertNotNull(refOption2.obligations.find { it.contribution == Contribution.RESPONDER && it.obligation == Obligation.REQUIRED })
    }
}