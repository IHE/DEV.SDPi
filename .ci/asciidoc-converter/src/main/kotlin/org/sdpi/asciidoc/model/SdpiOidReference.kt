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

// Oids for well known things defined in the specification. See
// Appendix 3.C Oid Identifiers in the specification for parent
// oid definitions.
// Source: https://wiki.ihe.net/index.php/PCD_OID_Management
enum class WellKnownOid(val id: String, val oid: String, val typeLabel: String, val description: String) {

    // Top-level oid for the SDPi specification.
    DEV_SDPi(
        "sdpi",
        "1.3.6.1.4.1.19376.1.6.2.10",
        "SDPi",
        "Parent OID for the SDPi specification"
    ),

    // Parent oid for all actors.
    DEV_ACTOR(
        "actors",
        DEV_SDPi.oid + ".3",
        "Actor",
        "Parent OID for all actors defined in SDPi profiles"
    ),

    // Parent oid for all transactions.
    DEV_TRANSACTION(
        "transactions",
        DEV_SDPi.oid + ".4",
        "Transaction",
        "Parent OID for all transactions defined in the SDPi specification"
    ),

    // Parent oid for all profiles
    DEV_PROFILE(
        "profiles",
        DEV_SDPi.oid + ".2",
        "Profile",
        "Parent OID for all SDPi profiles"
    ),

    // Parent oid for all profile actor options
    DEV_PROFILE_ACTOR_OPTIONS(
        "profile-actor-options",
        DEV_SDPi.oid + ".11",
        "Profile actor option",
        "Parent OID for all profile actor options defined in SDPi profiles"
    ),

    // Parent oid for content modules.
    DEV_CONTENT_MODULE(
        "content-modules",
        DEV_SDPi.oid + ".8",
        "Content module",
        "Parent OID for all content modules defined in the SDPi specification"
    ),

    // Parent oid for use case definitions
    DEV_USE_CASE_GLOBAL(
        "use-cases",
        DEV_SDPi.oid + ".9",
        "Use case",
        "Parent OID for general use cases defined in the SDPi specification"
    ),

    // Oid arc for use case support defined in profiles. Combine the profile parent
    // with this arc and the use-case support arc.
    DEV_USE_CASE_SUPPORT(
        "use-case-support",
         ".12",
        "Use case support",
        "Arc for use case support oids defined in SDPi specification profiles"
    ),

    // Parent oid for all requirements in the specification.
    DEV_REQUIREMENT(
        "requirements",
        DEV_SDPi.oid + ".10",
        "Requirement",
        "Parent OID for all requirements defined in the SDPi specification"
    )
}

fun parseOidId(strId: String): WellKnownOid? {
    return WellKnownOid.entries.firstOrNull { it.id == strId }
}
