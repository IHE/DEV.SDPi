package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.*
import org.sdpi.asciidoc.model.RequirementLevel
import org.sdpi.asciidoc.model.StructuralNodeWrapper
import org.sdpi.asciidoc.model.toSealed

/**
 * Checks expected requirement levels in SDPi requirements.
 *
 * Loops all paragraphs of sdpi_requirement blocks and checks if there is exactly one requirement level keyword that
 * equals the value of the attribute key sdpi_req_level.
 *
 * Exits with an error if
 *
 * - there are multiple different keywords
 * - if no keyword is found
 * - if more than one occurrence of the keyword is found
 */
class RequirementLevelProcessor : Treeprocessor() {
    override fun process(document: Document): Document {
        processBlock(document as StructuralNode)
        return document
    }

    private fun processBlock(block: StructuralNode) {
        block.toSealed().let { node ->
            when (node) {
                is StructuralNodeWrapper.SdpiRequirement -> {
                    processRequirement(node.wrapped)
                }
                else -> {
                    //logger.debug { "Ignore block of type '${block.context}'" }
                }
            }
        }

        block.blocks.forEach { processBlock(it) }
    }

    private fun processRequirement(block: Block) {
        val attributes = Attributes.create(block.attributes)
        val levelRaw = attributes[BlockAttribute.REQUIREMENT_LEVEL]
        checkNotNull(levelRaw) {
            ("Missing requirement level for SDPi requirement with id ${attributes[BlockAttribute.ID]}").also {
                logger.error { it }
            }
        }
        val level = resolveRequirementLevel(levelRaw)
        checkNotNull(level) {
            ("Invalid requirement level for SDPi requirement with id ${attributes[BlockAttribute.ID]}: $level").also {
                logger.error { it }
            }
        }

        val msgPrefix = "Check requirement level for #${attributes[BlockAttribute.ID]} (${level.keyword}):"
        var levelCount = 0
        block.blocks.forEach { reqBlock ->
            reqBlock.toSealed().let { childNode ->
                when (childNode) {
                    is StructuralNodeWrapper.Paragraph -> {
                        levelCount += childNode.wrapped.source
                            .split(" ")
                            .map { it.trim() }
                            .count { it == level.keyword }

                        RequirementLevel.values().filter { it != level }.forEach { notLevel ->
                            val notLevelCount = childNode.wrapped.source
                                .split(" ")
                                .map { it.trim() }
                                .count { it == notLevel.keyword }
                            check(notLevelCount == 0) {
                                ("$msgPrefix Requirement level keyword '${notLevel.keyword}' found").also {
                                    logger.error { it }
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        check(levelCount > 0) {
            ("$msgPrefix Requirement level keyword is missing").also {
                logger.error { it }
            }
        }

        check(levelCount == 1) {
            ("$msgPrefix Only one requirement level keyword allowed per requirement").also {
                logger.error { it }
            }
        }

        logger.info { "$msgPrefix Done" }
    }

    private companion object : Logging
}