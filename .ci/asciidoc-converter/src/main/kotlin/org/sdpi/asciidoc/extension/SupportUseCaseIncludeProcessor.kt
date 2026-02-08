package org.sdpi.asciidoc.extension

import org.asciidoctor.ast.ContentNode
import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.sdpi.asciidoc.UseCaseAttributes
import org.sdpi.asciidoc.model.SdpiProfileUseCaseReference
import org.sdpi.asciidoc.model.SdpiUseCaseReference
import org.sdpi.asciidoc.model.parseObligation

const val BLOCK_MACRO_NAME_SUPPORT_USE_CASE = "sdpi_support_use_case"

@Name(BLOCK_MACRO_NAME_SUPPORT_USE_CASE)
class SupportUseCaseIncludeProcessor : BlockMacroProcessor(BLOCK_MACRO_NAME_SUPPORT_USE_CASE) {
    private companion object : Logging

    // Keyed by profile id, this maps profiles to use case references.
    private val useCaseReferences = mutableMapOf<String, MutableList<SdpiProfileUseCaseReference>>()

    fun useCases() = useCaseReferences

    fun useCaseReferences(strProfile: String, strUseCaseId: String): List<SdpiProfileUseCaseReference>? {
        return useCaseReferences[strProfile]?.filter{it.useCaseReference.useCaseId == strUseCaseId}
    }

    override fun process(parent: StructuralNode, strActorId: String, attributes: MutableMap<String, Any>): Any? {
        val (strProfileId, strProfileOptionId, strUseCaseId) = findParentContext(parent)
        checkNotNull(strProfileId) {
            logger.error("$BLOCK_MACRO_NAME_SUPPORT_USE_CASE requires a ancestor block with the 'profile' role")
        }
        checkNotNull(strUseCaseId) {
            logger.error("$BLOCK_MACRO_NAME_SUPPORT_USE_CASE requires a ancestor block with the '${Roles.UseCaseSupport.SECTION_ROLE.key}' role")
        }

        check(strActorId.isNotEmpty()) {
            logger.error("$BLOCK_MACRO_NAME_SUPPORT_USE_CASE requires an actor target")
        }

        val strObligation = attributes[Roles.UseCaseSupport.OBLIGATION.key]?.toString()
        checkNotNull(strObligation) {
            logger.error("$BLOCK_MACRO_NAME_SUPPORT_USE_CASE requires an ${Roles.UseCaseSupport.OBLIGATION.key} attribute")
        }
        val obligation = parseObligation(strObligation)
        checkNotNull(obligation) {
            logger.error("$BLOCK_MACRO_NAME_SUPPORT_USE_CASE requires valid ${UseCaseAttributes.OBLIGATION.key} attribute")
        }


        val reference = SdpiUseCaseReference(strUseCaseId, strActorId, obligation)
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
                logger.error("Profile ${ref.profileId} (option=${ref.profileOptionId}) already references ${ref.useCaseReference.useCaseId} for actor ${ref.useCaseReference.actorId}")
            }
        }
        useCaseRefs.add(ref)

    }


    private fun findParentContext(parent: ContentNode): Triple<String?, String?, String?> {
        if (parent.hasRole(Roles.Profile.PROFILE.key)) {
            val strProfileId = parent.attributes[Roles.Profile.ID.key]?.toString()
            checkNotNull(strProfileId) {
                throw IllegalStateException("Profile has no id")
            }
            return Triple(strProfileId, null, null)
        } else if (parent.hasRole(Roles.Profile.PROFILE_OPTION.key)) {
            val strOptionId = parent.attributes[Roles.Profile.ID_PROFILE_OPTION.key]?.toString()
            val parentId = findParentContext(parent.parent)
            return Triple(parentId.first, strOptionId, null)
        } else if (parent.hasRole(Roles.UseCaseSupport.SECTION_ROLE.key)) {
            val strUseCaseId = parent.attributes[Roles.UseCaseSupport.USE_CASE_ID.key]?.toString()
            val parentId = findParentContext(parent.parent)
            return Triple(parentId.first, parentId.second, strUseCaseId)
        } else if (parent.parent != parent.document) {
            return findParentContext(parent.parent)
        } else {
            return Triple(null, null, null)
        }
    }
}
