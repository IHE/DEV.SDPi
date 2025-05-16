package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.*
import org.sdpi.asciidoc.model.*

class TreeInfoCollector(
    bibliography: BibliographyCollector,
    transactionActors: TransactionActorsProcessor,
    profiles: TransactionIncludeProcessor
) :
    Treeprocessor() {
    private val collector = SdpiInformationCollector(bibliography, transactionActors, profiles)

    fun info(): SdpiInformationCollector = collector

    override fun process(document: Document): Document {
        collector.process(document)

        return document
    }

}

class SdpiInformationCollector(
    private val bibliography: BibliographyCollector,
    private val transactionActors: TransactionActorsProcessor,
    private val profileTransactions: TransactionIncludeProcessor
) {
    private companion object : Logging

    private val requirements = mutableMapOf<Int, SdpiRequirement2>()

    private val requirementOwners = mutableMapOf<Int, BlockOwner?>()

    private val useCases = mutableMapOf<String, SdpiUseCase>()

    private val actors = mutableMapOf<String, SdpiActor>()

    private val transactions = mutableMapOf<String, SdpiTransaction>()

    private val profiles = mutableMapOf<String, SdpiProfile>()

    fun requirements(): Map<Int, SdpiRequirement2> = requirements

    fun profiles(): List<SdpiProfile> {
        return profiles.values.toList()
    }

    fun getProfile(strProfileId: String): SdpiProfile? {
        return profiles[strProfileId]
    }

    fun requirementOwners(): Map<Int, BlockOwner?> = requirementOwners

    fun useCases(): Map<String, SdpiUseCase> = useCases

    fun actors(): Map<String, SdpiActor> = actors

    fun transactions(): Map<String, SdpiTransaction> = transactions

    //fun profileTransactions(): Map<String, List<SdpiTransactionReference>> = profileTransactions.profiles()

    fun process(document: Document) {
        logger.info("Collecting sdpi information")
        processBlock(document as StructuralNode)

        collectActorRequirements()
    }


    private fun processBlock(block: StructuralNode) {
        if (block.hasRole("requirement")) {
            processRequirement(block)
        }

        if (block.hasRole(RoleNames.UseCase.FEATURE.key)) {
            processUseCase(block)
        }

        if (block.hasRole(RoleNames.Profile.PROFILE.key)) {
            processProfile(block)
        }

        if (block.hasRole("transaction")) {
            processTransaction(block)
        }

        for (child in block.blocks) {
            processBlock(child)
        }
    }

    // region Collect anchors
    private fun collectAnchors(block: StructuralNode) {

    }
    // endregion

    // region Profiles
    private fun processProfile(block: StructuralNode) {
        val strProfileId = block.attributes[RoleNames.Profile.ID.key]?.toString()
        checkNotNull(strProfileId) {
            logger.error("Profile block missing '${RoleNames.Profile.ID.key}'")
        }

        check(!profiles.containsKey(strProfileId)) {
            logger.error("Duplicate profile id found: $strProfileId")
        }

        val transactionRefs = profileTransactions.transactionReferences(strProfileId)
        val profileRefs = transactionRefs?.filter { it.profileOptionId == null }
            ?.map { it.transactionReference }
        val currentProfile = SdpiProfile(strProfileId, profileRefs)
        profiles[strProfileId] = currentProfile
        gatherProfileOptionTransactions(transactionRefs, currentProfile)

        for (child in block.blocks) {
            processProfileBlock(child, currentProfile, null)
        }
    }

    private fun gatherProfileOptionTransactions(
        transactionRefs: List<SdpiProfileTransactionReference>?,
        currentProfile: SdpiProfile
    ) {
        if (transactionRefs != null) {
            val groupedOption = transactionRefs.filter { it.profileOptionId != null }.groupBy { it.profileOptionId }
            for (go in groupedOption) {
                val strOptionId = go.key
                if (strOptionId != null) {
                    val optionTransactions = mutableListOf<SdpiTransactionReference>()
                    for (tr in go.value) {
                        optionTransactions.add(tr.transactionReference)
                    }
                    currentProfile.addOption(SdpiProfileOption(strOptionId, optionTransactions))
                }
            }
        }
    }

    private fun processProfileBlock(block: StructuralNode, profile: SdpiProfile, profileOption: SdpiProfileOption?) {
        if (block.hasRole("actor")) {
            processActor(block, profile)
        }
        val currentOption: SdpiProfileOption? = if (block.hasRole("profile-option")) {
            processProfileOption(block, profile)
        } else {
            profileOption
        }

        for (child in block.blocks) {
            processProfileBlock(child, profile, currentOption)
        }
    }

    private fun processProfileOption(block: StructuralNode, currentProfile: SdpiProfile): SdpiProfileOption {
        val strId = block.attributes["profile-option-id"]?.toString()
        checkNotNull(strId) {
            logger.error("Block with role 'profile-option' requires a 'profile-option-id")
        }

        val existingOption = currentProfile.options.firstOrNull{ it.id == strId}
        if (null != existingOption) {
            return existingOption
        }

        val newOption = SdpiProfileOption(strId, null)
        currentProfile.addOption(newOption)
        return newOption
    }
    // endregion

    //region Requirements
    private fun processRequirement(block: StructuralNode) {
        val nRequirementNumber = block.attributes["requirement-number"].toString().toInt()
        check(!requirements.contains(nRequirementNumber)) // check for duplicate.
        {
            val strRequirement = block.attributes["requirement-number"].toString()
            "Duplicate requirement #${strRequirement}: ${block.sourceLocation.path}:${block.sourceLocation.lineNumber}".also {
                logger.error { it }
            }
        }

        val strLocalId = String.format("R%04d", nRequirementNumber)
        val strGlobalId = block.attributes["global-id"]?.toString() ?: ""
        val aGroups: List<String> = getRequirementGroupMembership(block)
        val requirementLevel: RequirementLevel = getRequirementLevel(nRequirementNumber, block)
        val requirementType: RequirementType = getRequirementType(nRequirementNumber, block)
        val owner: RequirementContext? = getRequirementOwner(block)

        logger.info("Requirement: $nRequirementNumber")


        val specification = getSpecification(block, nRequirementNumber)
        checkSpecificationLevel(nRequirementNumber, requirementLevel, specification, block)

        when (requirementType) {
            RequirementType.TECH ->
                requirements[nRequirementNumber] = SdpiRequirement2.TechFeature(
                    nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
                )

            RequirementType.USE_CASE ->
                requirements[nRequirementNumber] = buildUseCaseRequirement(
                    block, nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
                )

            RequirementType.REF_ICS ->
                requirements[nRequirementNumber] = buildRefIcsRequirement(
                    block, nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
                )

            RequirementType.RISK_MITIGATION ->
                requirements[nRequirementNumber] = buildRiskMitigationRequirement(
                    block, nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
                )

            RequirementType.IHE_PROFILE -> buildIheProfileRequirement(
                block, nRequirementNumber,
                strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
            )
        }

        requirementOwners[nRequirementNumber] = gatherOwners(block.parent as StructuralNode)
    }

    private fun getRequirementOwner(block: ContentNode): RequirementContext? {
        if (block == block.document) {
            return null
        }

        for (strRole in block.roles) {
            val ownerType = parseOwnerFromRole(strRole)
            if (ownerType != null) {
                logger.info("Found owner of requirement: $ownerType")
                return RequirementContext(ownerType, block.id)
            }
        }

        return getRequirementOwner(block.parent)
    }

    private fun getSpecification(block: StructuralNode, nRequirementNumber: Int): RequirementSpecification {
        val normativeContent: MutableList<Content> = mutableListOf()
        val noteContent: MutableList<Content> = mutableListOf()
        val exampleContent: MutableList<Content> = mutableListOf()
        val relatedContent: MutableList<Content> = mutableListOf()
        val unstyledContent: MutableList<Content> = mutableListOf()

        for (child in block.blocks) {
            val strStyle = child.attributes["style"].toString()
            when (strStyle) {
                "NORMATIVE" -> {
                    normativeContent.addAll(getContent_Obj(child))
                }

                "NOTE" -> {
                    noteContent.addAll(getContent_Obj(child))
                }

                "RELATED" -> {
                    relatedContent.addAll(getContent_Obj(child))
                }

                "EXAMPLE" -> {
                    exampleContent.addAll(getContent_Obj(child))
                }

                "example" -> {
                    logger.warn("Notes should be an example block in requirement #${nRequirementNumber}. In the future this will be an error")
                    noteContent.addAll(getContent_Obj(child))
                }

                else -> {
                    logger.warn("Unstyled content in requirement #${nRequirementNumber}. In the future this will be an error")

                    unstyledContent.addAll(getContent_Obj(child))
                }
            }
        }

        // treat plain paragraphs as the normative content for
        // backwards compatibility.
        if (normativeContent.isEmpty()) {
            logger.warn("${block.sourceLocation} is missing normative content section; using unstyled paragraphs. This will be an error in the future")
            normativeContent.addAll(unstyledContent)
        }

        return RequirementSpecification(normativeContent, noteContent, exampleContent, relatedContent)
    }

    private fun checkSpecificationLevel(
        nRequirementNumber: Int,
        expectedLevel: RequirementLevel,
        specification: RequirementSpecification,
        block: StructuralNode
    ) {
        val normativeStatement = specification.normativeContent
        val nMayCount = countKeyword(RequirementLevel.MAY.keyword, normativeStatement)
        val nShallCount = countKeyword(RequirementLevel.SHALL.keyword, normativeStatement)
        val nShouldCount = countKeyword(RequirementLevel.SHOULD.keyword, normativeStatement)

        logger.info("Check requirement level for #$nRequirementNumber ($expectedLevel)")

        check(normativeStatement.isNotEmpty())
        {
            "${block.sourceLocation} requirement #$nRequirementNumber is missing the required normative statement".also { logger.error { it } }
        }

        when (expectedLevel) {
            RequirementLevel.MAY -> {
                check(nMayCount == 1)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should have exactly one lower-case may keyword, not $nMayCount".also { logger.error { it } }
                }

                check(nShallCount == 0 && nShouldCount == 0)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should not have any shall ($nShallCount) or should ($nShouldCount)".also { logger.error { it } }
                }
            }

            RequirementLevel.SHOULD -> {
                check(nShouldCount == 1)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should have exactly one lower-case should keyword, not $nShouldCount".also { logger.error { it } }
                }

                check(nShallCount == 0 && nMayCount == 0)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should not have any shall ($nShallCount) or may ($nMayCount)".also { logger.error { it } }
                }
            }

            RequirementLevel.SHALL -> {
                check(nShallCount == 1)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should have exactly one lower-case shall keyword, not $nShallCount".also { logger.error { it } }
                }

                check(nShouldCount == 0 && nMayCount == 0)
                {
                    "${getLocation(block)} requirement #$nRequirementNumber should not have any should ($nShouldCount) or may ($nMayCount)".also { logger.error { it } }
                }
            }
        }

    }

    private fun buildUseCaseRequirement(
        block: StructuralNode, nRequirementNumber: Int,
        strLocalId: String, strGlobalId: String,
        requirementLevel: RequirementLevel, owner: RequirementContext?, aGroups: List<String>,
        specification: RequirementSpecification
    ): SdpiRequirement2 {
        val useCaseHeader: ContentNode = getUseCaseNode(nRequirementNumber, block.parent)
        val useCaseId = useCaseHeader.attributes[RequirementAttributes.UseCase.ID.key]
        checkNotNull(useCaseId) {
            "Can't find use case id for requirement #${nRequirementNumber}".also {
                logger.error { it }
            }
        }

        block.attributes[RequirementAttributes.UseCase.ID.key] = useCaseId.toString()
        return SdpiRequirement2.UseCase(
            nRequirementNumber,
            strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification, useCaseId.toString()
        )
    }

    private fun buildRefIcsRequirement(
        block: StructuralNode, nRequirementNumber: Int,
        strLocalId: String, strGlobalId: String,
        requirementLevel: RequirementLevel, owner: RequirementContext?, aGroups: List<String>,
        specification: RequirementSpecification
    ): SdpiRequirement2 {
        val strStandardId = block.attributes[RequirementAttributes.RefIcs.ID.key]?.toString()
        checkNotNull(strStandardId) {
            "Missing standard id for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val section = block.attributes[RequirementAttributes.RefIcs.SECTION.key]
        val requirement = block.attributes[RequirementAttributes.RefIcs.REQUIREMENT.key]
        check(section != null || requirement != null) {
            "At least one of ${RequirementAttributes.RefIcs.SECTION.key} or ${RequirementAttributes.RefIcs.REQUIREMENT.key} is required for requirement #${nRequirementNumber}".also {
                logger.error(
                    it
                )
            }
        }

        val bibEntry = bibliography.findEntry(strStandardId)
        checkNotNull(bibEntry)
        {
            "${getLocation(block)} bibliography entry for $strStandardId is missing".also { logger.error { it } }
        }
        val strRefSource = bibEntry.source

        val strSection = section?.toString() ?: ""
        val strRequirement = requirement?.toString() ?: ""
        return SdpiRequirement2.ReferencedImplementationConformanceStatement(
            nRequirementNumber,
            strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification,
            strStandardId, strRefSource, strSection, strRequirement
        )

    }

    private fun buildRiskMitigationRequirement(
        block: StructuralNode, nRequirementNumber: Int,
        strLocalId: String, strGlobalId: String,
        requirementLevel: RequirementLevel, owner: RequirementContext?, aGroups: List<String>,
        specification: RequirementSpecification
    ): SdpiRequirement2 {
        val sesType = block.attributes[RequirementAttributes.RiskMitigation.SES_TYPE.key]
        checkNotNull(sesType) {
            "Missing ses type for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val strSesType = sesType.toString()
        val parsedSesType = RiskMitigationType.entries.firstOrNull { it.keyword == strSesType }
        checkNotNull(parsedSesType) {
            "Invalid ses type ($strSesType) for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val testability = block.attributes[RequirementAttributes.RiskMitigation.TESTABILITY.key]
        checkNotNull(testability) {
            "Missing test type for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        val strTest = testability.toString()
        val parsedTestability = RiskMitigationTestability.entries.firstOrNull { it.keyword == strTest }
        checkNotNull(parsedTestability) {
            "Invalid test type ($strTest) for requirement #${nRequirementNumber}".also { logger.error(it) }
        }

        return SdpiRequirement2.RiskMitigation(
            nRequirementNumber,
            strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification,
            parsedSesType, parsedTestability
        )

    }

    private fun buildIheProfileRequirement(
        block: StructuralNode, nRequirementNumber: Int,
        strLocalId: String, strGlobalId: String,
        requirementLevel: RequirementLevel, owner: RequirementContext?, aGroups: List<String>,
        specification: RequirementSpecification
    ): SdpiRequirement2 {
        check(false) {
            "Currently unsupported".also { logger.error(it) }
        }

        return SdpiRequirement2.TechFeature(
            nRequirementNumber,
            strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
        )
    }

    private fun getContent_Obj(block: StructuralNode): Collection<Content> {
        val contents: MutableList<Content> = mutableListOf()

        val strContext = block.context
        when (strContext) {
            "paragraph" -> {
                getBlockContent_Obj(contents, block, 0)
            }

            "admonition", "example" -> {
                for (child in block.blocks) {
                    getBlockContent_Obj(contents, child, 0)
                }
            }

            else -> {
                logger.error("Unknown content type ${block.javaClass}")
            }
        }

        return contents
    }

    private fun getBlockContent_Obj(contents: MutableList<Content>, block: StructuralNode, nLevel: Int) {
        when (block) {
            is org.asciidoctor.ast.List -> {
                val items: MutableList<Content> = mutableListOf()
                for (item: StructuralNode in block.items) {
                    getBlockContent_Obj(items, item, nLevel)
                }
                if (block.context == "olist") {
                    contents.add(Content.OrderedList(items.toList()))
                } else {
                    contents.add(Content.UnorderedList(items.toList()))
                }
            }

            is org.asciidoctor.ast.ListItem -> {
                contents.add(Content.ListItem(block.marker, block.source))
            }

            is org.asciidoctor.ast.Block -> {
                val lines: MutableList<String> = mutableListOf()
                for (strLine in block.lines) {
                    lines.add(strLine)
                }

                if (block.context == "listing") {
                    val strTitle: String = block.title ?: ""
                    contents.add(Content.Listing(strTitle, lines))
                } else {
                    contents.add(Content.Block(lines))
                }
            }

            else -> {
                logger.info("${getLocation(block)} unknown: ${block.javaClass}")
            }
        }
    }

    /**
     * Retrieves the list of groups the requirement belongs to (if any).
     */
    private fun getRequirementGroupMembership(block: StructuralNode): List<String> {
        return getRequirementGroups(block.attributes[RequirementAttributes.Common.GROUPS.key])
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
    private fun getRequirementType(requirementNumber: Int, block: StructuralNode): RequirementType {
        var strType = block.attributes[RequirementAttributes.Common.TYPE.key]

        // For now, assume tech feature by default for backwards compatibility.
        if (strType == null) {
            strType = "tech_feature"
            logger.warn("${getLocation(block)}, requirement type missing for #$requirementNumber, assuming $strType. In the future this will be an error.")
        }

        checkNotNull(strType) {
            ("Missing ${RequirementAttributes.Common.TYPE.key} attribute for SDPi requirement #$requirementNumber [${
                getLocation(
                    block
                )
            }]").also {
                logger.error { it }
            }
        }
        val reqType = RequirementType.entries.firstOrNull { it.keyword == strType }
        checkNotNull(reqType) {
            ("Invalid requirement type '${strType}' for SDPi requirement #$requirementNumber [${getLocation(block)}]").also {
                logger.error { it }
            }
        }

        return reqType
    }

    private fun getUseCaseNode(requirementNumber: Int, parent: ContentNode): ContentNode {
        var node: ContentNode? = parent
        while (node != null) {
            val attr = node.attributes
            for (k in attr.keys) {
                if (k == UseCaseAttributes.ID.key) {
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

    private fun processUseCase(block: StructuralNode) {
        val strUseCaseId = block.attributes[UseCaseAttributes.ID.key].toString()
        val strTitle = block.title
        val strAnchor = block.id

        val specBlocks: MutableList<StructuralNode> = mutableListOf()
        gatherUseCaseBlocks(block, specBlocks)

        val backgroundContent: MutableList<GherkinStep> = mutableListOf()
        val scenarios: MutableList<UseCaseScenario> = mutableListOf()
        var iBlock = 0
        while (iBlock < specBlocks.count()) {
            val useCaseBlock = specBlocks[iBlock]
            if (useCaseBlock.hasRole(RoleNames.UseCase.BACKGROUND.key)) {
                backgroundContent.addAll(getSteps(useCaseBlock))
            }

            if (useCaseBlock.hasRole(RoleNames.UseCase.SCENARIO.key)) {
                val oTitle = useCaseBlock.attributes["sdpi_scenario"]
                checkNotNull(oTitle)
                {
                    "${getLocation(useCaseBlock)} missing required scenario title".also { logger.error { it } }
                }

                val iStepBlock = iBlock + 1
                check(iStepBlock < specBlocks.count() && specBlocks[iStepBlock].hasRole(RoleNames.UseCase.STEPS.key))
                {
                    "${getLocation(useCaseBlock)} missing steps for scenario $oTitle".also { logger.error { it } }
                }
                val stepBlock = specBlocks[iStepBlock]
                val scenarioSteps = getSteps(stepBlock)
                scenarios.add(UseCaseScenario(oTitle.toString(), scenarioSteps))
            }


            ++iBlock
        }

        val spec = UseCaseSpecification(backgroundContent, scenarios)
        useCases[strUseCaseId] = SdpiUseCase(strUseCaseId, strTitle, strAnchor, spec)
    }

    private fun gatherUseCaseBlocks(block: StructuralNode, specBlocks: MutableList<StructuralNode>) {
        for (child in block.blocks) {
            val strRole = child.role
            when (strRole) {
                RoleNames.UseCase.BACKGROUND.key -> specBlocks.add(child)
                RoleNames.UseCase.SCENARIO.key -> {
                    specBlocks.add(child)
                    gatherUseCaseBlocks(child, specBlocks)
                }

                RoleNames.UseCase.STEPS.key -> specBlocks.add(child)
                else -> gatherUseCaseBlocks(child, specBlocks)
            }
        }
    }

    private fun getSteps(block: StructuralNode): List<GherkinStep> {
        val steps: MutableList<GherkinStep> = mutableListOf()

        val reType = Regex("[*](?<type>[a-zA-Z]+)[*]\\s+(?<description>.*)")
        for (child in block.blocks) {
            check(child is org.asciidoctor.ast.Block)
            {
                "${getLocation(child)} steps must be paragraphs".also { logger.error { it } }
            }
            for (strLine in child.lines) {
                val mType = reType.find(strLine)
                checkNotNull(mType)
                {
                    "${getLocation(child)} step invalid format".also { logger.error { it } }
                }

                val oType = mType.groups["type"]?.value
                checkNotNull(oType)
                {
                    "${getLocation(child)} step missing type".also { logger.error { it } }
                }

                val oDescription = mType.groups["description"]?.value
                checkNotNull(oDescription)
                {
                    "${getLocation(child)} step missing description".also { logger.error { it } }
                }

                val stepType = resolveStepType(oType.toString())
                checkNotNull(stepType)
                {
                    "${getLocation(child)} invalid step type".also { logger.error { it } }
                }

                steps.add(GherkinStep(stepType, oDescription.toString()))
            }
        }

        return steps
    }

    //endregion

    //region actors
    private fun processActor(block: StructuralNode, profile: SdpiProfile) {
        val strId = block.id
        val strRefText = block.reftext

        check(!actors.contains(strId)) // check for duplicate.
        {
            "Duplicate actor #${strId} ($strRefText)".also {
                logger.error { it }
            }
        }

        val newActor = SdpiActor(strId, strRefText, profile.profileId)
        actors[strId] = newActor
        profile.addActor(newActor)
    }

    private fun collectActorRequirements() {
        for (req in requirements) {
            val nRequirementId = req.key
            val actorIds = req.value.specification.getActorIds()
            for (strActorId in actorIds) {
                val actor = actors[strActorId]
                checkNotNull(actor) {
                    logger.error("Requirement ${req.value.localId} contains unknown actor $strActorId")
                }
                actor.requirements.add(nRequirementId)
            }
        }
    }
    //endregion

    //region transactions

    private fun processTransaction(block: StructuralNode) {
        val strAnchor = block.id
        val strLabel = block.title
        val reExtractTitleElements = Regex("""\S+\s+(.*?)\s+\[(.+?)]""")
        val mrTitleElements = reExtractTitleElements.find(strLabel)

        checkNotNull(mrTitleElements) {
            "Can't get title and transaction id from $strLabel".also {
                logger.error { it }
            }
        }
        val (strTitle, strTransactionId) = mrTitleElements.destructured

        check(!transactions.contains(strTransactionId)) // check for duplicate.
        {
            "Duplicate transaction #${strTransactionId} ($strLabel)".also {
                logger.error { it }
            }
        }

        val actorRoles = transactionActors.transactionActors()[strTransactionId]
        transactions[strTransactionId] = SdpiTransaction(strAnchor, strTransactionId, strTitle, actorRoles)
    }
    //endregion

}