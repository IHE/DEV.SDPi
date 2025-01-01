package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.*
import org.sdpi.asciidoc.model.*

class SdpiInformationCollector : Treeprocessor()
{
    private companion object : Logging

    private val requirements = mutableMapOf<Int, SdpiRequirement2>()

    private val useCases = mutableMapOf<String, SdpiUseCase>()

    fun requirements(): Map<Int, SdpiRequirement2> = requirements

    fun useCases(): Map<String, SdpiUseCase> = useCases

    override fun process(document: Document): Document
    {
        processBlock(document as StructuralNode)

        return document
    }

    private fun processBlock(block : StructuralNode)
    {
        if (block.hasRole("requirement"))
        {
            processRequirement(block)
        }

        if (block.hasRole("use-case"))
        {
            processUseCase(block)
        }

        for(child in block.blocks)
        {
            processBlock(child)
        }
    }

    //region Requirements
    private fun processRequirement(block: StructuralNode)
    {
        val nRequirementNumber = block.attributes["requirement-number"].toString().toInt()
        check(!requirements.contains(nRequirementNumber)) // check for duplicate.
        {
            val strRequirement = block.attributes["requirement-number"].toString();
            "Duplicate requirement #${strRequirement}: ${block.sourceLocation.path}:${block.sourceLocation.lineNumber}".also {
                logger.error { it }
            }
        }

        val strLocalId = String.format("R%04d", nRequirementNumber)
        val strGlobalId = block.attributes["id"].toString()
        val aGroups: List<String> = getRequirementGroupMembership(block)
        val requirementLevel: RequirementLevel = getRequirementLevel(nRequirementNumber, block)
        val requirementType: RequirementType = getRequirementType(nRequirementNumber, block)

        logger.info("Requirement: $nRequirementNumber")


        val specification = getSpecification(block)
        checkSpecificationLevel(nRequirementNumber, requirementLevel, specification, block)

        when (requirementType)
        {
            RequirementType.TECH ->
                requirements[nRequirementNumber] = SdpiRequirement2.TechFeature(nRequirementNumber,
                    strLocalId, strGlobalId,  requirementLevel, aGroups, specification )

            RequirementType.USE_CASE ->
                requirements[nRequirementNumber] = buildUseCaseRequirement(block, nRequirementNumber,
                    strLocalId, strGlobalId,  requirementLevel, aGroups, specification)

            RequirementType.REF_ICS ->
                requirements[nRequirementNumber] = buildRefIcsRequirement(block, nRequirementNumber,
                    strLocalId, strGlobalId,  requirementLevel, aGroups, specification)

            RequirementType.RISK_MITIGATION ->
                requirements[nRequirementNumber] = buildRiskMitigationRequirement(block, nRequirementNumber,
                    strLocalId, strGlobalId,  requirementLevel, aGroups, specification)

            RequirementType.IHE_PROFILE -> buildIheProfileRequirement(block, nRequirementNumber,
                strLocalId, strGlobalId,  requirementLevel, aGroups, specification)
        }
    }

    private fun getSpecification(block: StructuralNode) : RequirementSpecification
    {
        val normativeContent: MutableList<Content> = mutableListOf()
        val noteContent: MutableList<Content> = mutableListOf()
        val exampleContent: MutableList<Content> = mutableListOf()
        val relatedContent: MutableList<Content> = mutableListOf()

        for (child in block.blocks)
        {
            val strStyle = child.attributes["style"].toString()
            when (strStyle)
            {
                "NORMATIVE" ->
                {
                    normativeContent.addAll(getContent_Obj(child))
                }

                "NOTE" ->
                {
                    logger.info("  Note:")
                    noteContent.addAll(getContent_Obj(child))
                }

                "RELATED" ->
                {
                    logger.info("  Related:")
                    relatedContent.addAll(getContent_Obj(child))
                }

                "EXAMPLE" ->
                {
                    logger.info("  Example:")
                    exampleContent.addAll(getContent_Obj(child))
                }
            }
        }

        return RequirementSpecification(normativeContent, noteContent, exampleContent, relatedContent)
    }

    private fun checkSpecificationLevel(nRequirementNumber: Int, expectedLevel : RequirementLevel, specification: RequirementSpecification, block: StructuralNode)
    {
        val normativeStatement = specification.normativeContent
        val nMayCount = countKeyword(RequirementLevel.MAY.keyword, normativeStatement)
        val nShallCount = countKeyword(RequirementLevel.SHALL.keyword, normativeStatement)
        val nShouldCount = countKeyword(RequirementLevel.SHOULD.keyword, normativeStatement)

        logger.info("Check requirement level for #$nRequirementNumber ($expectedLevel)")

        when (expectedLevel)
        {
            RequirementLevel.MAY ->
            {
                check( nMayCount == 1)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should have exactly one may keyword, not $nMayCount".also { logger.error{it} }
                }

                check( nShallCount == 0 && nShouldCount == 0)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should not have any shall ($nShallCount) or should ($nShouldCount)".also { logger.error{it} }
                }
            }

            RequirementLevel.SHOULD ->
            {
                check( nShouldCount == 1)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should have exactly one should keyword, not $nShouldCount".also { logger.error{it} }
                }

                check( nShallCount == 0 && nMayCount == 0)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should not have any shall ($nShallCount) or may ($nMayCount)".also { logger.error{it} }
                }
            }

            RequirementLevel.SHALL ->
            {
                check( nShallCount == 1)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should have exactly one shall keyword, not $nShallCount".also { logger.error{it} }
                }

                check( nShouldCount == 0 && nMayCount == 0)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should not have any should ($nShouldCount) or may ($nMayCount)".also { logger.error{it} }
                }
            }
        }

    }

    private fun buildUseCaseRequirement(block: StructuralNode, nRequirementNumber: Int,
                                        strLocalId : String, strGlobalId : String,
                                        requirementLevel: RequirementLevel, aGroups : List<String>,
                                        specification : RequirementSpecification) : SdpiRequirement2
    {
        val useCaseHeader: ContentNode = getUseCaseNode(nRequirementNumber, block.parent)
        val useCaseId = useCaseHeader.attributes[RequirementAttributes.UseCase.ID.key]
        checkNotNull(useCaseId) {
            "Can't find use case id for requirement #${nRequirementNumber}".also {
                logger.error { it }
            }
        }

        block.attributes[RequirementAttributes.UseCase.ID.key] = useCaseId.toString()
        return SdpiRequirement2.UseCase(nRequirementNumber,
            strLocalId, strGlobalId,  requirementLevel, aGroups, specification, useCaseId.toString() )
    }

    private fun buildRefIcsRequirement(block: StructuralNode, nRequirementNumber: Int,
                                        strLocalId : String, strGlobalId : String,
                                        requirementLevel: RequirementLevel, aGroups : List<String>,
                                        specification : RequirementSpecification) : SdpiRequirement2
    {
        val standardId = block.attributes[RequirementAttributes.RefIcs.ID.key]
        checkNotNull(standardId){
            "Missing standard id for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val section = block.attributes[RequirementAttributes.RefIcs.SECTION.key]
        val requirement = block.attributes[RequirementAttributes.RefIcs.REQUIREMENT.key]
        check(section != null || requirement != null) {
            "At least one of ${RequirementAttributes.RefIcs.SECTION.key} or ${RequirementAttributes.RefIcs.REQUIREMENT.key} is required for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val strSection = section?.toString() ?: ""
        val strRequirement = requirement?.toString() ?: ""
        return SdpiRequirement2.ReferencedImplementationConformanceStatement(nRequirementNumber,
            strLocalId, strGlobalId,  requirementLevel, aGroups, specification,
            standardId.toString(), strSection, strRequirement )

    }

    private fun buildRiskMitigationRequirement(block: StructuralNode, nRequirementNumber: Int,
                                       strLocalId : String, strGlobalId : String,
                                       requirementLevel: RequirementLevel, aGroups : List<String>,
                                       specification : RequirementSpecification) : SdpiRequirement2
    {
        val sesType = block.attributes[RequirementAttributes.RiskMitigation.SES_TYPE.key]
        checkNotNull(sesType){
            "Missing ses type for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val strSesType = sesType.toString()
        val parsedSesType = RiskMitigationType.entries.firstOrNull {it.keyword == strSesType }
        checkNotNull(parsedSesType){
            "Invalid ses type ($strSesType) for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val testability = block.attributes[RequirementAttributes.RiskMitigation.TESTABILITY.key]
        checkNotNull(testability){
            "Missing test type for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val strTest = testability.toString()
        val parsedTestability = RiskMitigationTestability.entries.firstOrNull{it.keyword == strTest}
        checkNotNull(parsedTestability){
            "Invalid test type ($strTest) for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        return SdpiRequirement2.RiskMitigation(nRequirementNumber,
            strLocalId, strGlobalId,  requirementLevel, aGroups, specification,
            parsedSesType, parsedTestability)

    }

    private fun buildIheProfileRequirement(block: StructuralNode, nRequirementNumber: Int,
                                               strLocalId : String, strGlobalId : String,
                                               requirementLevel: RequirementLevel, aGroups : List<String>,
                                               specification : RequirementSpecification) : SdpiRequirement2
    {
        check(false) {
            "Currently unsupported".also { logger.error(it) }
        }

        return SdpiRequirement2.TechFeature(nRequirementNumber,
            strLocalId, strGlobalId,  requirementLevel, aGroups, specification )
    }

    private fun getContent(block : StructuralNode): Collection<String>
    {
        val contents: MutableList<String> = mutableListOf()

        val strContext = block.context
        when (strContext)
        {
            "paragraph" ->
            {
                getBlockContent(contents, block, 0)
            }
            "admonition", "example" ->
            {
                for (child in block.blocks)
                {
                    getBlockContent(contents, child, 0)
                }
            }
            else ->
            {
                logger.error("Unknown content type ${block.javaClass}")
            }
        }

        return contents
    }

    private fun getBlockContent(contents: MutableList<String>, block : StructuralNode, nLevel : Int)
    {
        val strIndent = "  ".repeat(nLevel)

        when (block)
        {
            is org.asciidoctor.ast.List ->
            {
                logger.info("${strIndent}List:")
                contents.add("<list>")
                for (item : StructuralNode in block.items)
                {
                    getBlockContent(contents, item, nLevel)
                }
                contents.add("</list>")
            }

            is org.asciidoctor.ast.ListItem ->
            {
                logger.info("${strIndent}Item: ${block.marker} ${block.text}")
                contents.add("${strIndent}${block.marker} ${block.text}")
            }

            is org.asciidoctor.ast.Block ->
            {
                logger.info("${strIndent}Block:")
                for(strLine in block.lines)
                {
                    logger.info("$strIndent  Line: $strLine")
                    contents.add(strLine)
                }
            }

            else ->
            {
                logger.info("$strIndent}Unknown: ${block.javaClass}")
            }
        }
    }

    private fun getContent_Obj(block : StructuralNode): Collection<Content>
    {
        val contents: MutableList<Content> = mutableListOf()

        val strContext = block.context
        when (strContext)
        {
            "paragraph" ->
            {
                getBlockContent_Obj(contents, block, 0)
            }
            "admonition", "example" ->
            {
                for (child in block.blocks)
                {
                    getBlockContent_Obj(contents, child, 0)
                }
            }
            else ->
            {
                logger.error("Unknown content type ${block.javaClass}")
            }
        }

        return contents
    }

    private fun getBlockContent_Obj(contents: MutableList<Content>, block : StructuralNode, nLevel : Int)
    {
        val strIndent = "  ".repeat(nLevel)

        when (block)
        {
            is org.asciidoctor.ast.List ->
            {
                val items : MutableList<Content> = mutableListOf()
                for (item : StructuralNode in block.items)
                {
                    getBlockContent_Obj(items, item, nLevel)
                }
                if (block.context == "olist")
                {
                    contents.add(Content.Content_OrderedList(items.toList()))
                }
                else
                {
                    contents.add(Content.Content_UnorderedList(items.toList()))
                }
            }

            is org.asciidoctor.ast.ListItem ->
            {
                contents.add(Content.Content_ListItem(block.marker, block.text))
            }

            is org.asciidoctor.ast.Block ->
            {
                logger.info("${strIndent}Block: (${block.context})")
                val lines : MutableList<String> = mutableListOf()
                for(strLine in block.lines)
                {
                    lines.add(strLine)
                }

                if (block.context == "listing")
                {
                    val strTitle : String = block.title ?: ""
                    contents.add(Content.Content_Listing(strTitle, lines))
                }
                else
                {
                    contents.add(Content.Content_Block( lines))
                }
            }

            else ->
            {
                logger.info("$strIndent}Unknown: ${block.javaClass}")
            }
        }
    }

    /**
     * Retrieves the list of groups the requirement belongs to (if any).
     */
    private fun getRequirementGroupMembership(block: StructuralNode): List<String> {
        return  getRequirementGroups( block.attributes[RequirementAttributes.Common.GROUPS.key])
    }

    /**
     * Retrieve the requirement level
     */
    private fun getRequirementLevel(requirementNumber: Int, block: StructuralNode): RequirementLevel {
        val strLevel = block.attributes[RequirementAttributes.Common.LEVEL.key]
        checkNotNull(strLevel) {
            ("Missing ${RequirementAttributes.Common.LEVEL.key} attribute for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }
        val reqLevel = resolveRequirementLevel(strLevel.toString())
        checkNotNull(reqLevel) {
            ("Invalid requirement level '${strLevel}' for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }

        return reqLevel
    }

    /**
     * Retrieve the requirement type
     */
    private fun getRequirementType(requirementNumber: Int, block: StructuralNode) : RequirementType
    {
        val strType = block.attributes[RequirementAttributes.Common.TYPE.key]
        checkNotNull(strType) {
            ("Missing ${RequirementAttributes.Common.TYPE.key} attribute for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }
        val reqType = RequirementType.entries.firstOrNull { it.keyword == strType }
        checkNotNull(reqType) {
            ("Invalid requirement type '${strType}' for SDPi requirement #$requirementNumber").also {
                logger.error { it }
            }
        }

        return reqType
    }

    private fun getUseCaseNode(requirementNumber: Int, parent: ContentNode): ContentNode
    {
        var node: ContentNode? = parent
        while (node != null) {
            val attr = node.attributes
            for ( k in attr.keys)
            {
                if (k == UseCaseAttributes.ID.key)
                {
                    return node
                }
            }
            node = node.parent
        }

        checkNotNull(node) {
            "Can't find use case in parents for requirement #${requirementNumber}".also {
                logger.error { it }
            }
        }

    }

    //endregion

    //region Use case

    private fun processUseCase(block : StructuralNode)
    {
        val strUseCaseId = block.attributes[UseCaseAttributes.ID.key].toString()
        val strTitle = block.title
        val strAnchor = block.id

        useCases[strUseCaseId] = SdpiUseCase(strUseCaseId, strTitle, strAnchor)
    }

    //endregion

}