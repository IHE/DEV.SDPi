package org.sdpi.asciidoc.model

import kotlinx.serialization.Serializable

@Serializable
data class SdpiOidReference(val root: WellKnownOid, val oid: String, val description: String, val anchor: String) :
    Comparable<SdpiOidReference> {

    override fun compareTo(other: SdpiOidReference): Int {
        val partsA = oid.split(".").map { it.toInt() }
        val partsB = other.oid.split(".").map { it.toInt() }
        val maxLen = maxOf(partsA.size, partsB.size)

        for (i in 0 until maxLen) {
            val numA = partsA.getOrElse(i) { 0 }
            val numB = partsB.getOrElse(i) { 0 }
            val cmp = numA.compareTo(numB)
            if (cmp != 0) return cmp
        }

        return 0
    }

}

enum class WellKnownOid(val id: String, val oid: String, val typeLabel: String, val description: String) {

    // Source: https://wiki.ihe.net/index.php/PCD_OID_Management
    DEV_ACTOR("actors", "1.3.6.1.4.1.19376.1.6.3", "Actor", "Parent OID for the DEV Actor"),

    // Source: https://wiki.ihe.net/index.php/PCD_OID_Management
    DEV_TRANSACTION("transactions", "1.3.6.1.4.1.19376.1.6.4", "Transaction", "Parent OID for the DEV Transaction"),
}

fun parseOidId(strId: String): WellKnownOid? {
    return WellKnownOid.entries.firstOrNull { it.id == strId }
}
