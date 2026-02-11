package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.UseCaseAttributes
import org.sdpi.asciidoc.findIdFromParent
import org.sdpi.asciidoc.findProfileId
import org.sdpi.asciidoc.model.SdpiProfileUseCaseReference
import org.sdpi.asciidoc.model.SdpiUseCaseReference
import org.sdpi.asciidoc.model.parseObligation

const val BLOCK_MACRO_NAME_INCLUDE_USE_CASE = "sdpi_include_use_case"

@Name(BLOCK_MACRO_NAME_INCLUDE_USE_CASE)
class UseCaseIncludeProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_INCLUDE_USE_CASE) {
    private companion object : Logging

    // Keyed by profile id, this maps profiles to use case references.
    private val useCaseReferences = mutableMapOf<String, MutableList<SdpiProfileUseCaseReference>>()

    fun useCases() = useCaseReferences

    fun useCaseReferences(strProfile: String): List<SdpiProfileUseCaseReference>? {
        return useCaseReferences[strProfile]
    }

    override fun process(parent: StructuralNode, strUseCaseId: String, attributes: MutableMap<String, Any>): Any? {
        val (strProfileId, strProfileOptionId) = findProfileId(parent)
        checkNotNull(strProfileId) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_USE_CASE requires a ancestor block within the 'profile' role")
        }
        val strActor = attributes[UseCaseAttributes.ACTOR.key]?.toString() ?: findIdFromParent(
            parent,
            "actor",
            UseCaseAttributes.ACTOR.key
        )
        checkNotNull(strActor) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_USE_CASE requires an ${UseCaseAttributes.ACTOR.key} attribute or parent container")
        }

        val strObligation = attributes[UseCaseAttributes.OBLIGATION.key]?.toString()
        checkNotNull(strObligation) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_USE_CASE requires an ${UseCaseAttributes.OBLIGATION.key} attribute")
        }
        val obligation = parseObligation(strObligation)
        checkNotNull(obligation) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_USE_CASE requires valid ${UseCaseAttributes.OBLIGATION.key} attribute")
        }


        val reference = SdpiUseCaseReference(strUseCaseId, strActor, obligation)
        val profileReference = SdpiProfileUseCaseReference(strProfileId, strProfileOptionId, reference)

        addReference(profileReference)

        return null
    }

    private fun addReference(ref: SdpiProfileUseCaseReference) {
        var useCaseRefs = useCaseReferences[ref.profileId]
        if (useCaseRefs == null) {
            useCaseRefs = mutableListOf()
            useCaseReferences[ref.profileId] = useCaseRefs
        } else {
            val bDuplicate = useCaseRefs.any {
                (it.useCaseReference.useCaseId == ref.useCaseReference.useCaseId)
                        && (it.profileOptionId == ref.profileOptionId)
                        && (it.useCaseReference.actorId == ref.useCaseReference.actorId)
            }
            check(!bDuplicate) {
                logger.error("Profile ${ref.profileId} (option=${ref.profileOptionId}) already references ${ref.useCaseReference.useCaseId}")
            }
        }
        useCaseRefs.add(ref)

    }
}
