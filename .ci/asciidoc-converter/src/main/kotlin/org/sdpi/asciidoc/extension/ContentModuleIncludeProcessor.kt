package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.ContentModuleAttributes
import org.sdpi.asciidoc.findIdFromParent
import org.sdpi.asciidoc.findProfileId
import org.sdpi.asciidoc.model.*

const val BLOCK_MACRO_NAME_INCLUDE_CONTENT_MODULE = "sdpi_include_content_module"

@Name(BLOCK_MACRO_NAME_INCLUDE_CONTENT_MODULE)
class ContentModuleIncludeProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_INCLUDE_CONTENT_MODULE) {
    private companion object : Logging

    // Keyed by profile id, this maps profiles to use case references.
    private val references = mutableMapOf<String, MutableList<SdpiProfileContentModuleRef>>()

    fun contentModules() = references

    fun contentModuleReferences(strProfile: String): List<SdpiProfileContentModuleRef>? {
        return references[strProfile]
    }

    override fun process(
        parent: StructuralNode,
        strContentModuleId: String,
        attributes: MutableMap<String, Any>
    ): Any? {
        val (strProfileId, strProfileOptionId) = findProfileId(parent)
        checkNotNull(strProfileId) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_CONTENT_MODULE requires a ancestor block within the 'profile' role")
        }

        val strActor = attributes[ContentModuleAttributes.ACTOR.key]?.toString() ?: findIdFromParent(
            parent,
            "actor",
            ContentModuleAttributes.ACTOR.key
        )
        checkNotNull(strActor) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_CONTENT_MODULE requires an ${ContentModuleAttributes.ACTOR.key} attribute or parent container")
        }

        val strObligation = attributes[ContentModuleAttributes.OBLIGATION.key]?.toString()
        checkNotNull(strObligation) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_CONTENT_MODULE requires an ${ContentModuleAttributes.OBLIGATION.key} attribute")
        }
        val obligation = parseObligation(strObligation)
        checkNotNull(obligation) {
            logger.error("$BLOCK_MACRO_NAME_INCLUDE_CONTENT_MODULE requires valid ${ContentModuleAttributes.OBLIGATION.key} attribute")
        }


        val reference = SdpiContentModuleRef(strContentModuleId, strActor, obligation)
        val profileReference = SdpiProfileContentModuleRef(strProfileId, strProfileOptionId, reference)

        addReference(profileReference)

        return null
    }

    private fun addReference(ref: SdpiProfileContentModuleRef) {
        var existing = references[ref.profileId]
        if (existing == null) {
            existing = mutableListOf<SdpiProfileContentModuleRef>()
            references[ref.profileId] = existing
        } else {
            val bDuplicate = existing.any {
                (it.ref.contentModuleId == ref.ref.contentModuleId)
                        && (it.profileOptionId == ref.profileOptionId)
                        && (it.ref.actorId == ref.ref.actorId)
            }
            check(!bDuplicate) {
                logger.error("Profile ${ref.profileId} (option=${ref.profileOptionId}) already references ${ref.ref.contentModuleId} content module for ${ref.ref.actorId}")
            }
        }

        logger.info("${ref.profileId} (${ref.profileOptionId}) references content module ${ref.ref.contentModuleId} for ${ref.ref.actorId}")
        existing.add(ref)

    }
}
