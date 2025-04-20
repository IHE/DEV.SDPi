package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable

@Serializable
class SdpiProfile(val profileId: String) {
    private val referencedTransactions = mutableListOf<SdpiTransactionReference>()

    fun addTransactionReference(transaction: SdpiTransactionReference) {
        referencedTransactions.add(transaction)
    }

    fun referencedTransactions() = referencedTransactions
}