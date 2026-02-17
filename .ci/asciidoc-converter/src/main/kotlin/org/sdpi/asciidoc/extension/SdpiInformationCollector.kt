package org.sdpi.asciidoc.extension

import org.apache.logging.log4j.kotlin.Logging
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Treeprocessor
import org.sdpi.asciidoc.*
import org.sdpi.asciidoc.model.*

/*
A tree processor that gathers information as the document is processed.
For example, we identify all the profiles, the actors and options they
define. Information gathered is validated, to ensure no two requirements
have the same id, for example.
 */
class SdpiInformationCollector(
    private val bibliography: BibliographyCollector,
    private val transactionActors: TransactionActorsProcessor,
    private val profileTransactions: TransactionIncludeProcessor,
    private val profileUseCases: SupportUseCaseIncludeProcessor,
    private val profileContentModuleReferences: ContentModuleIncludeProcessor,
    private val externalStandardsProcessor : ExternalStandardProcessor
) : Treeprocessor() {
    private companion object : Logging

    private val requirements = mutableMapOf<Int, SdpiRequirement2>()

    private val useCases = mutableMapOf<String, SdpiUseCase>()

    private val actors = mutableMapOf<String, SdpiActor>()

    // Actors can be referenced by id as well as their actor-id. This maps
    // ids to actor-ids.
    private val actorAliases = mutableMapOf<String, String>()

    private val transactions = mutableMapOf<String, SdpiTransaction>()

    // Content modules discovered in the document, keyed by the content module id.
    private val contentModules = mutableMapOf<String, SdpiContentModule>()

    private val profiles = mutableMapOf<String, SdpiProfile>()

    fun requirements(): Map<Int, SdpiRequirement2> = requirements

    fun profiles(): List<SdpiProfile> {
        return profiles.values.toList()
    }

    fun getProfile(strProfileId: String): SdpiProfile? {
        return profiles[strProfileId]
    }

    fun useCases(): Map<String, SdpiUseCase> = useCases

    fun transactions(): Map<String, SdpiTransaction> = transactions

    fun contentModules(): Map<String, SdpiContentModule> = contentModules

    fun findActor(strActor: String): SdpiActor? {
        val strPrimaryId = actorAliases[strActor]
        if (strPrimaryId == null) {
            logger.info("Can't find primary id for actor $strActor. Known aliases are:")
            for (strAlias in actorAliases.keys) {
                logger.info("  $strAlias => ${actorAliases[strAlias]}")
            }
            return null
        }

        return actors[strPrimaryId]
    }

    fun dumpActorAliases() {
        println("Actor aliases")
        println("=============")
        for (alias in actorAliases) {
            println("${alias.key} => ${alias.value}")
        }
    }

    override fun process(document: Document): Document {
        logger.info("Collecting sdpi information")
        processBlock(document as StructuralNode)

        gatherRequirementMetadata()
        linkActorsToTransactionReferences()

        validateTransactionRefs()
        validateUseCaseRefs()
        validateContentModuleRefs()
        validateTransactions()
        validateRequirements()
        validateActors()

        validateObjectIds()

        // This is more proof-of-concept rather than information
        // we want to include in the export. Currently, it doesn't
        // gather requirements across actor groupings.
        // collectActorRequirements()

        return document
    }

    private fun processBlock(block: StructuralNode) {
        if (block.hasRole("requirement")) {
            processRequirement(block)
        }

        if (block.hasRole(Roles.UseCase.FEATURE.key)) {
            processUseCase(block)
        }

        if (block.hasRole(Roles.Profile.PROFILE.key)) {
            processProfile(block)
        }

        if (block.hasRole(Roles.ContentModule.SECTION_ROLE.key)) {
            processContentModule(block)
        }

        if (block.hasRole("transaction")) {
            processTransaction(block)
        }

        for (child in block.blocks) {
            processBlock(child)
        }
    }

    // region Profiles
    private fun processProfile(block: StructuralNode) {
        val strProfileId = block.attributes[Roles.Profile.ID.key]?.toString()
        checkNotNull(strProfileId) {
            logger.error("Profile block missing '${Roles.Profile.ID.key}'")
        }

        check(!profiles.containsKey(strProfileId)) {
            logger.error("Duplicate profile id found: $strProfileId")
        }

        val oids = getOids(block, "Profile $strProfileId", WellKnownOid.DEV_PROFILE)
        val transactionRefs = profileTransactions.transactionReferences(strProfileId)
        val profileTransactionRefs = transactionRefs?.filter { !it.isOptioned() }
            ?.map { it.transactionReference }

        val contentModuleRefs = profileContentModuleReferences.contentModuleReferences(strProfileId)
        val profileContentModuleRefs = contentModuleRefs?.filter { it.profileOptionId == null }?.map { it.ref }

        val strAnchor = block.id
        val strLabel = parseProfileTitle(block)

        val currentProfile =
            SdpiProfile(
                strProfileId,
                oids,
                strAnchor,
                strLabel,
                profileTransactionRefs,
                profileContentModuleRefs
            )
        profiles[strProfileId] = currentProfile
        for (child in block.blocks) {
            processProfileBlock(child, currentProfile, null)
        }

        gatherProfileOptionTransactions(transactionRefs, currentProfile)
        gatherProfileOptionContentModules(contentModuleRefs, currentProfile)
    }

    private fun parseProfileTitle(block: StructuralNode): String {

        if (block.reftext != null) {
            return block.reftext
        }

        val strDocTitle = block.title
        val reTitle = Regex("""^\d+([.:]\d+)*\s+(.*\s–\s)(.*)$""")
        val match = reTitle.find(strDocTitle)
        val strTitle = match?.groups?.get(3)?.value ?: strDocTitle
        checkNotNull(strTitle) {
            logger.error("Profile title '$strDocTitle' is not formatted correctly")
        }
        return strTitle
    }

    private fun parseProfileOptionTitle(block: StructuralNode): String {

        if (block.reftext != null) {
            return block.reftext
        }

        val strDocTitle = block.title
        val reTitle = Regex("""^\d+([.:]\d+)*\s+(.*)$""")
        val match = reTitle.find(strDocTitle)
        val strTitle = match?.groups?.get(2)?.value
        checkNotNull(strTitle) {
            logger.error("Profile option title '$strDocTitle' is not formatted correctly")
        }
        return strTitle
    }

    private fun processProfileBlock(block: StructuralNode, profile: SdpiProfile, profileOption: SdpiProfileOption?) {
        if (block.hasRole(Roles.Actor.SECTION_ROLE.key)) {
            processActor(block, profile)
        }
        if (block.hasRole(Roles.Actor.ALIAS.key)) {
            processActorAlias(block)
        }

        // Not currently used; part of confusion between profile-actor-options,
        // actor options and profile options (hint: there are only profile-actor-options).
        /*
        if (block.hasRole(Roles.Actor.OPTION.key)) {
            processActorOption(block, profile)
        }*/

        if (block.hasRole(Roles.UseCaseSupport.SECTION_ROLE.key)) {
            processUseCaseSupport(block, profile)
        }

        val currentOption: SdpiProfileOption? = if (block.hasRole(Roles.Profile.PROFILE_OPTION.key)) {
            processProfileOption(block, profile)
        } else {
            profileOption
        }

        for (child in block.blocks) {
            processProfileBlock(child, profile, currentOption)
        }
    }

    private fun processActor(block: StructuralNode, profile: SdpiProfile) {
        val strAnchor = block.id
        val strId = block.attributes[Roles.Actor.ID.key]?.toString()
        checkNotNull(strId) {
            logger.error("Block with ${Roles.Actor.SECTION_ROLE.key} role requires an ${Roles.Actor.ID.key}")
        }


        val strLabel = block.reftext ?: block.title
        logger.info("Found actor $strId => $strLabel")

        val reExtractTitleElements = Regex("""^\d+([.:]\d+)*\s+(.*)""")
        val mrTitleElements = reExtractTitleElements.find(strLabel)
        val strTitle = mrTitleElements?.groups?.get(2)?.value ?: strLabel
        checkNotNull(strTitle) {
            logger.error("No label for actor $strId")
        }

        check(!actors.contains(strId)) // check for duplicate.
        {
            "Duplicate actor #${strId} ($strTitle)".also {
                logger.error { it }
            }
        }

        val oids = getOids(block, "Actor $strId", WellKnownOid.DEV_ACTOR)
        val requiredGroupings = getRequiredGroupings(block, "Actor $strId")

        actorAliases[strId] = strId // Self alias for easy lookup
        actorAliases["actor_$strId"] = strId // common alternative name.
        val newActor = SdpiActor(strId, oids, strTitle, profile.profileId, strAnchor, requiredGroupings)
        actors[strId] = newActor
        profile.addActor(newActor)
    }

    private fun getRequiredGroupings(block: StructuralNode, strContext: String): List<ActorGrouping>? {

        val strGrouping = block.attributes[Roles.Actor.GROUPING.key]?.toString() ?: return null

        val actorGroupings = mutableListOf<ActorGrouping>()
        val astrGroupings = strGrouping.split(",")
        for (strGroup in astrGroupings) {
            val match =  ActorGrouping.GROUPING_REGEX.matchEntire(strGroup)
            checkNotNull(match) {
                "Invalid actor grouping for $strContext".also {
                    logger.error{it}
                }
            }

            val strActorId = match.groupValues[1]
            val strOptionId = match.groupValues[2].ifBlank{null}
            actorGroupings.add(ActorGrouping(strActorId, strOptionId))
        }

        return actorGroupings
    }


    private fun processActorOption(block: StructuralNode, profile: SdpiProfile) {
        val strAnchor = block.id
        val strId = block.attributes[Roles.Actor.OPTION_ID.key]?.toString()
        checkNotNull(strId) {
            logger.error("Block with ${Roles.Actor.OPTION.key} role requires an ${Roles.Actor.OPTION_ID.key}")
        }
        val strTitle = getTitleFrom(block)

        val transactionRefs = profileTransactions.transactionReferences(profile.profileId)
        val actorOptionTransactionRefs =
            transactionRefs?.filter { it.profileOptionId == null && it.actorOptionId == strId }
                ?.map { it.transactionReference }

        val option = SdpiActorOption(strId, strTitle, strAnchor, actorOptionTransactionRefs)
        profile.addActorOption(option)
    }

    private fun processActorAlias(block: StructuralNode) {
        val strId = block.attributes[Roles.Actor.ID.key]?.toString()
        checkNotNull(strId) {
            logger.error("Block with ${Roles.Actor.ALIAS.key} role requires an ${Roles.Actor.ID.key}")
        }

        val strAlias = block.id
        checkNotNull(strAlias) {
            logger.error("block with ${Roles.Actor.ALIAS.key} role requires an id")
        }

        check(!actorAliases.containsKey(strAlias)) {
            logger.error("Alias $strAlias already exists")
        }

        logger.info("Found actor alias: $strAlias ==> $strId")

        actorAliases[strAlias] = strId
    }

    private fun collectActorRequirements() {
        logger.info("Collecting actor requirements...")
        for (req in requirements) {
            val nRequirementId = req.key
            val actorIds = req.value.specification.getActorIds()
            for (strActorId in actorIds) {
                val actor = findActor(strActorId)
                checkNotNull(actor) {
                    logger.error("Requirement ${req.value.localId} contains unknown actor $strActorId")
                }
                actor.requirements.add(nRequirementId)
            }
        }
    }

    private fun processUseCaseSupport(block: StructuralNode, profile: SdpiProfile) {
        val strUseCaseId = block.attributes[Roles.UseCaseSupport.USE_CASE_ID.key]?.toString()
        checkNotNull(strUseCaseId) {
            logger.error("Use case in profile ${profile.profileId} requires ${Roles.UseCaseSupport.USE_CASE_ID.key} attribute")
        }
        val strAnchor = block.id
        val parentOids = profile.oids.map{ "$it.12" }
        val oids = getOids(block, "Profile use case support", parentOids)
        val useCaseSupport = profileUseCases.useCaseReferences(profile.profileId, strUseCaseId)
        if (useCaseSupport != null) {
            val obligations = useCaseSupport.map{it -> UseCaseObligations(it.useCaseReference.actorId, it.useCaseReference.obligation, it.profileOptionId)}
            val supports = SdpiUseCaseSupport(strUseCaseId, oids,strAnchor, obligations)
            profile.addUseCaseSupport(supports)
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
                    val profileOption = currentProfile.findOption(strOptionId)
                    checkNotNull(profileOption) {
                        logger.error("Profile ${currentProfile.profileId} does not have an option $strOptionId for transactions")
                    }
                    go.value.forEach { profileOption.add(it.transactionReference) }
                }
            }
        }
    }

    private fun gatherProfileOptionContentModules(
        contentModuleRefs: List<SdpiProfileContentModuleRef>?,
        currentProfile: SdpiProfile
    ) {
        if (contentModuleRefs != null) {
            val groupedOption = contentModuleRefs.filter { it.profileOptionId != null }.groupBy { it.profileOptionId }
            for (go in groupedOption) {
                val strOptionId = go.key
                if (strOptionId != null) {
                    val profileOption = currentProfile.findOption(strOptionId)
                    checkNotNull(profileOption) {
                        logger.error("Profile ${currentProfile.profileId} does not have an option $strOptionId for content modules")
                    }
                    go.value.forEach { profileOption.add(it.ref) }
                }
            }
        }
    }

    private fun processProfileOption(block: StructuralNode, currentProfile: SdpiProfile): SdpiProfileOption {
        val strId = block.attributes["profile-option-id"]?.toString()
        checkNotNull(strId) {
            logger.error("Block with role 'profile-option' requires a 'profile-option-id")
        }

        val existingOption = currentProfile.options.firstOrNull { it.id == strId }
        if (null != existingOption) {
            return existingOption
        }

        val strAnchor = block.id
        val strLabel = parseProfileOptionTitle(block)

        val oids = getOids(block, "Profile option $strId", WellKnownOid.DEV_PROFILE_ACTOR_OPTIONS)

        val newOption = SdpiProfileOption(strId, oids, strAnchor, strLabel)
        currentProfile.addOption(newOption)
        return newOption
    }
    // endregion

    // region Content modules
    private fun processContentModule(block: StructuralNode) {
        val strContentModuleId = block.attributes[Roles.ContentModule.ID.key]?.toString()
        checkNotNull(strContentModuleId) {
            logger.error("Content module block (id=${block.id} missing '${Roles.ContentModule.ID.key}'")
        }

        check(!contentModules.containsKey(strContentModuleId)) {
            logger.error("Duplicate content module id found: $strContentModuleId")
        }

        val strLabel = parseContentModuleTitle(block.title)
        val strAnchor = block.id

        val oids = getOids(block, "Content module $strContentModuleId", WellKnownOid.DEV_CONTENT_MODULE)

        contentModules[strContentModuleId] = SdpiContentModule(strContentModuleId, oids, strLabel, strAnchor)
    }

    private fun parseContentModuleTitle(strDocText: String): String {
        val reTitle = Regex("""^\d+([.:]\d+)*\s+(.*)$""")
        val match = reTitle.find(strDocText)
        val strTitle = match?.groups?.get(2)?.value
        checkNotNull(strTitle) {
            logger.error("Content module title '$strDocText' is not formatted correctly")
        }
        return strTitle
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

        val newRequirement = buildRequirement(nRequirementNumber, block)
        linkSupportForExternalStandards(newRequirement)

        requirements[nRequirementNumber] = newRequirement
    }

    private fun buildRequirement(nRequirementNumber: Int, block: StructuralNode) : SdpiRequirement2 {
        val strLocalId = String.format("R%04d", nRequirementNumber)
        val strGlobalId = block.attributes["global-id"]?.toString() ?: ""
        val aGroups: List<String> = getRequirementGroupMembership(block)
        val requirementLevel: RequirementLevel = getRequirementLevel(nRequirementNumber, block)
        val requirementType: RequirementType = getRequirementType(nRequirementNumber, block)
        val owner: RequirementContext? = getRequirementOwner(block)

        //println("Requirement: $nRequirementNumber")


        val specification = getSpecification(block, nRequirementNumber)
        checkSpecificationLevel(nRequirementNumber, requirementLevel, specification, block)

        when (requirementType) {
            RequirementType.TECH ->
                return SdpiRequirement2.TechFeature(
                    nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, getMaxOccurrence(block), owner, aGroups, specification
                )

            RequirementType.USE_CASE ->
                return buildUseCaseRequirement(
                    block, nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
                )

            RequirementType.REF_ICS ->
                return buildRefIcsRequirement(
                    block, nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
                )

            RequirementType.RISK_MITIGATION ->
                return buildRiskMitigationRequirement(
                    block, nRequirementNumber,
                    strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
                )

            RequirementType.IHE_PROFILE ->
                return buildIheProfileRequirement(
                block, nRequirementNumber,
                strLocalId, strGlobalId, requirementLevel, owner, aGroups, specification
            )
        }
    }

    private fun getMaxOccurrence(block: StructuralNode): Int? {
        val strMaxOccurrence = block.attributes[BlockAttribute.MAX_OCCURRENCE.key] ?: return null

        val nMaxOccurrence = strMaxOccurrence.toString().toInt()
        return nMaxOccurrence
    }

    private fun getRequirementOwner(block: ContentNode): RequirementContext? {
        if (block == block.document) {
            return null
        }

        for (strRole in block.roles) {
            val ownerType = parseOwnerFromRole(strRole)
            if (ownerType != null) {
                val strId = when (ownerType) {
                    OwningContext.PROFILE -> block.attributes[Roles.Profile.ID.key]?.toString()
                    OwningContext.PROFILE_OPTION -> block.attributes[Roles.Profile.ID_PROFILE_OPTION.key]?.toString()
                    OwningContext.CONTENT_MODULE -> block.attributes[Roles.ContentModule.ID.key]?.toString()
                    OwningContext.GATEWAY -> block.attributes[Roles.Gateway.ID.key]?.toString()
                    OwningContext.PROTOCOL -> block.attributes[Roles.Protocol.ID.key]?.toString()
                    OwningContext.USE_CASE -> block.attributes[UseCaseAttributes.ID.key]?.toString()
                }
                checkNotNull(strId) {
                    logger.error("Owner missing id")
                }
                //println("Found owner of requirement: $ownerType = $strId")
                return RequirementContext(ownerType, strId)
            }
        }

        return getRequirementOwner(block.parent)
    }

    private fun getSpecification(block: StructuralNode, nRequirementNumber: Int): RequirementSpecification {
        val normativeContent: MutableList<Content> = mutableListOf()
        val noteContent: MutableList<Content> = mutableListOf()
        val exampleContent: MutableList<Content> = mutableListOf()
        val relatedContent: MutableList<Content> = mutableListOf()
        val unStyledContent: MutableList<Content> = mutableListOf()


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
                    logger.warn("Notes should be an example block in requirement #${nRequirementNumber}. ")
                    throw IllegalStateException()
                    //noteContent.addAll(getContent_Obj(child))
                }

                else -> {
                    logger.error("Un-styled content in requirement #${nRequirementNumber}.")
                    throw IllegalStateException()
                    //unStyledContent.addAll(getContent_Obj(child))
                }
            }
        }

        if (normativeContent.isEmpty()) {
            logger.warn("${block.sourceLocation} is missing normative content section; using un-styled paragraphs.")
            //normativeContent.addAll(unStyledContent)
            throw IllegalStateException()
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
            strLocalId, strGlobalId, requirementLevel, getMaxOccurrence(block), owner, aGroups, specification, useCaseId.toString()
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
            strLocalId, strGlobalId, requirementLevel, getMaxOccurrence(block), owner, aGroups, specification,
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
            strLocalId, strGlobalId, requirementLevel, getMaxOccurrence(block), owner, aGroups, specification,
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
            strLocalId, strGlobalId, requirementLevel, getMaxOccurrence(block), owner, aGroups, specification
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

    private fun linkSupportForExternalStandards(req: SdpiRequirement2)
    {
        for(c in req.specification.relatedContent) {
            val matches = c.getIdMatches(ExternalRequirementReference.REQUIREMENT_REF_REGEX, 0)
            for(strRef in matches) {
                val mReference = ExternalRequirementReference.REQUIREMENT_REF_REGEX.find(strRef)
                checkNotNull(mReference) {
                    logger.error("Unexpected match failure in parseExternalRequirements")
                }
                val strRefId = mReference.groupValues[1]
                val strArguments = mReference.groupValues[2]
                val arguments = parseArgs(strArguments)
                val strStandardId = arguments["standard-id"]
                checkNotNull(strStandardId){
                    logger.error("Missing standard argument in $strRef")
                }
                val strComment = arguments["comment"]
                //println("------ $strRef: standard = $strStandardId, requirement = $strRefId, comment=$strComment")
                val requirement = createExternalRef(strStandardId, strRefId)
                requirement.addSupport(ExternalRequirementSupport(req, strComment))
            }
        }
    }

    private fun parseArgs(s: String): Map<String, String> =
        s.split(ExternalRequirementReference.REQUIREMENT_REF_ARGS).associate { arg ->
            val (key, rawValue) = ExternalRequirementReference.REQUIREMENT_ARG_PAIRS.matchEntire(arg.trim())!!.destructured
            val value = rawValue.trim().removeSurrounding("\"").removeSurrounding("'")
            key to value
        }

    private fun createExternalRef(strStandardId: String, strRequirementId: String): ExternalRequirement {
        val standard = externalStandardsProcessor.getStandard(strStandardId)
        checkNotNull(standard){
            logger.error("Unknown standard id: $strStandardId")
        }
        val requirement = standard.getRequirement(strRequirementId)
        checkNotNull(requirement) {
            logger.error("Unknown requirement '$strRequirementId' for standard '$strStandardId'")
        }

        return requirement
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
            logger.error("${getLocation(block)}, requirement type missing for #$requirementNumber.")
            throw IllegalStateException()
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
        val strTitle = parseUseCaseTitle(block)
        val strAnchor = block.id

        val specBlocks: MutableList<StructuralNode> = mutableListOf()
        gatherUseCaseBlocks(block, specBlocks)

        val useCaseOids = getOids(block, "Use case $strUseCaseId", WellKnownOid.DEV_USE_CASE_GLOBAL)

        val backgroundContent: MutableList<GherkinStep> = mutableListOf()
        val scenarios: MutableList<UseCaseScenario> = mutableListOf()
        var iBlock = 0
        while (iBlock < specBlocks.count()) {
            val useCaseBlock = specBlocks[iBlock]
            if (useCaseBlock.hasRole(Roles.UseCase.BACKGROUND.key)) {
                backgroundContent.addAll(getSteps(useCaseBlock))
            }

            if (useCaseBlock.hasRole(Roles.UseCase.SCENARIO.key)) {
                val oTitle = useCaseBlock.attributes["sdpi_scenario"]
                checkNotNull(oTitle)
                {
                    "${getLocation(useCaseBlock)} missing required scenario title".also { logger.error { it } }
                }

                val scenarioOids = getOids(useCaseBlock, "Use case $strUseCaseId scenario ${oTitle.toString()}", useCaseOids)

                val iStepBlock = iBlock + 1
                check(iStepBlock < specBlocks.count() && specBlocks[iStepBlock].hasRole(Roles.UseCase.STEPS.key))
                {
                    "${getLocation(useCaseBlock)} missing steps for scenario $oTitle".also { logger.error { it } }
                }
                val stepBlock = specBlocks[iStepBlock]
                val scenarioSteps = getSteps(stepBlock)
                scenarios.add(UseCaseScenario(scenarioOids, oTitle.toString(), scenarioSteps))
            }


            ++iBlock
        }

        val spec = UseCaseSpecification(backgroundContent, scenarios)
        useCases[strUseCaseId] = SdpiUseCase(strUseCaseId,useCaseOids, strTitle, strAnchor, spec)
    }

    private fun parseUseCaseTitle(block: StructuralNode): String {
        if (block.reftext != null) {
            return block.reftext
        }

        val strDocTitle = block.title
        val reTitle = Regex(""":\s+(.*) +\(""")
        val match = reTitle.find(strDocTitle)
        val strTitle = match?.groups?.get(1)?.value
        checkNotNull(strTitle) {
            logger.error("Use case title '$strDocTitle' is not formatted correctly")
        }

        return strTitle
    }

    private fun gatherUseCaseBlocks(block: StructuralNode, specBlocks: MutableList<StructuralNode>) {
        for (child in block.blocks) {
            val strRole = child.role
            when (strRole) {
                Roles.UseCase.BACKGROUND.key -> specBlocks.add(child)
                Roles.UseCase.SCENARIO.key -> {
                    specBlocks.add(child)
                    gatherUseCaseBlocks(child, specBlocks)
                }

                Roles.UseCase.STEPS.key -> specBlocks.add(child)
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

    //region transactions

    private fun processTransaction(block: StructuralNode) {
        val strAnchor = block.id
        val strLabel = block.title
        val reExtractTitleElements = Regex("""[\d:.]+\s+(.*?)\s+\[.*?]""")
        val mrTitleElements = reExtractTitleElements.find(strLabel)

        checkNotNull(mrTitleElements) {
            "Can't get title and transaction id from $strLabel".also {
                logger.error { it }
            }
        }
        val strTitle = mrTitleElements.groupValues[1]

        val strTransactionId = block.attributes[Roles.Transaction.TRANSACTION_ID.key]?.toString()
        checkNotNull(strTransactionId) {
            logger.error("Transaction id on block $strTitle is required")
        }

        val strDefaultLeaf = getDefaultTransactionOid(strTransactionId)
        val oids = getOids(block, "Transaction $strTransactionId", WellKnownOid.DEV_TRANSACTION, strDefaultLeaf)


        check(!transactions.contains(strTransactionId)) // check for duplicate.
        {
            "Duplicate transaction #${strTransactionId} ($strLabel)".also {
                logger.error { it }
            }
        }

        val actorRoles = transactionActors.transactionActors()[strTransactionId]
        transactions[strTransactionId] = SdpiTransaction(strTransactionId, oids, strTitle, strAnchor, actorRoles)
    }

    private fun getDefaultTransactionOid(strTransactionId: String): String? {
        if (strTransactionId.startsWith("DEV-")) {
            val strDefaultLeaf = strTransactionId.substring(4)
            if (strDefaultLeaf.toIntOrNull() != null) {
                return ".$strDefaultLeaf"
            }
        }
        return null

    }
    //endregion

    //region Populate metadata

    private fun gatherRequirementMetadata() {
        for (req in requirements.values) {
            req.gatherActors(actorAliases)
        }
    }

    private fun linkActorsToTransactionReferences() {
        for (profile in profiles.values) {
            linkActorsToTransactionReferences(profile.transactionReferences)

            for (option in profile.options) {
                linkActorsToTransactionReferences(option.transactionReferences)
            }
        }
    }

    private fun linkActorsToTransactionReferences(transactionReferences: List<SdpiTransactionReference>?) {
        if (transactionReferences != null) {
            for (ref in transactionReferences) {
                val transaction = transactions[ref.transactionId]
                if (transaction != null) {
                    for (obl in ref.obligations) {
                        if (obl.actorId == null) {
                            val contrib = obl.contribution
                            val role = transaction.actorRoles?.firstOrNull { it.contribution == contrib }
                            if (role != null) {
                                val strActorId = role.actorId
                                obl.actorId = strActorId
                            }
                        }
                    }
                }
            }
        }
    }

    //endregion

    // region Helpers
    private fun getOids(
        block: StructuralNode,
        strContext: String,
        root: WellKnownOid,
        strDefaultLeaf: String? = null
    ): List<String> {
        val strLeafArcs = block.attributes[BlockAttribute.LEAF_ARC.key]?.toString() ?: strDefaultLeaf
        checkNotNull(strLeafArcs) {
            logger.error("$strContext requires an ${BlockAttribute.LEAF_ARC.key}")
        }

        val blockOids = mutableListOf<String>()
        for (strOid in strLeafArcs.split(',')) {
            if (strOid.startsWith('.')) {
                blockOids.add("${root.oid}$strOid")
            } else {
                blockOids.add(strOid)
            }
        }
        return blockOids
    }

    private fun getOids(
        block: StructuralNode,
        strContext: String,
        parentOids: List<String>
    ): List<String> {
        val strLeafArcs = block.attributes[BlockAttribute.LEAF_ARC.key]?.toString()
        checkNotNull(strLeafArcs) {
            logger.error("$strContext requires an ${BlockAttribute.LEAF_ARC.key}")
        }

        val blockOids = mutableListOf<String>()
        for (strOid in strLeafArcs.split(',')) {
            if (strOid.startsWith('.')) {
                for (strParentOid in parentOids) {
                    val strBlockOid = "${strParentOid}$strOid"
                    blockOids.add(strBlockOid)
                }
            } else {
                blockOids.add(strOid)
            }
        }
        return blockOids
    }


    // endregion

    //region Validation

    private fun validateTransactionRefs() {
        for (profile in profiles.values) {
            if (null != profile.transactionReferences) {
                validateTransactionRefs(profile.profileId, profile.transactionReferences)
            }
            for (profileOption in profile.options) {
                validateTransactionRefs(profileOption.id, profileOption.transactionReferences)
            }
        }
    }

    private fun validateUseCaseRefs() {
        for (profile in profiles.values) {
            validateUseCaseRefs(profile, profile.useCasesSupported())
        }
    }

    private fun validateContentModuleRefs() {
        for (profile in profiles.values) {
            if (null != profile.contentModuleReferences) {
                validateContentModuleRefs(profile.profileId, profile.contentModuleReferences)
            }
        }
    }

    private fun validateTransactions() {
        for (trans in transactions.values) {
            if (trans.actorRoles != null) {
                for (role in trans.actorRoles) {
                    val actor = actors[role.actorId]
                    checkNotNull(actor) {
                        logger.error("The actor ${role.actorId} in transaction ${trans.id} can't be found")
                    }
                }
            }
        }
    }

    private fun validateRequirements() {
        for (req in requirements.values) {
            for (strActorId in req.actors()) {
                val actor = actors[strActorId]
                checkNotNull(actor) {
                    logger.warn("Requirement ${req.localId} references unknown actor $strActorId")
                }
            }
        }
    }

    private fun validateTransactionRefs(strProfileId: String, transactionRefs: List<SdpiTransactionReference>) {
        for (ref in transactionRefs.filter { it.placeholderName == null }) {
            val transaction = transactions[ref.transactionId]
            checkNotNull(transaction) {
                logger.error("Profile '${strProfileId} references an unknown transaction ${ref.transactionId}")
            }
        }
    }

    private fun validateUseCaseRefs(profile: SdpiProfile, refs: List<SdpiUseCaseSupport>) {
        for (ref in refs) {
            val useCase = useCases[ref.useCaseId]
            checkNotNull(useCase) {
                logger.error("Profile '${profile.profileId}' references an unknown use case ${ref.useCaseId}")
            }

            for(obligation in ref.obligations) {
                val strActorId = obligation.actorId

                val actor = actors[strActorId]
                checkNotNull(actor) {
                    logger.error("The actor '${strActorId} in a ${ref.useCaseId} use-case reference in profile '${profile.profileId}' can't be found")
                }

                val strProfileOption = obligation.profileOptionId
                if (strProfileOption != null) {
                    val option = profile.options.firstOrNull{it.id == strProfileOption}
                    checkNotNull(option) {
                        logger.error("The option '${strProfileOption} in a ${ref.useCaseId} use-case reference in profile '${profile.profileId}' can't be found")
                    }
                }
            }
        }
    }

    private fun validateContentModuleRefs(strProfileId: String, refs: List<SdpiContentModuleRef>) {
        for (ref in refs) {
            // If the reference isn't deferred then the target content module must exist.
            if (ref.placeholderName == null) {
                val contentModule = contentModules[ref.contentModuleId]
                checkNotNull(contentModule) {
                    logger.error("Profile '${strProfileId} references an unknown content module ${ref.contentModuleId}")
                }
            }

            val actor = actors[ref.actorId]
            checkNotNull(actor) {
                logger.error("The actor '${ref.actorId} in a ${ref.contentModuleId} content module reference in profile $strProfileId can't be found")
            }
        }
    }

    private fun validateActors() {
        for(profile in profiles.values) {
            for(actor in profile.actorReferences()) {
                if (actor.requiredActorGroupings != null) {
                    //println("***** Checking actor grouping for ${actor.id}")
                    for(grouping in actor.requiredActorGroupings) {
                        val strParent = grouping.actorId
                        val strOption = grouping.optionId
                        check(strParent != actor.id) {
                            logger.error("The actor ${actor.id} is grouped with itself, which is not allowed")
                        }
                        check(actors.containsKey(strParent)) {
                            logger.error("The actor ${actor.id} is grouped with unknown actor $strParent")
                        }
                        if (strOption != null) {
                            check(optionExists(strOption)) {
                                logger.error("The actor ${actor.id} is optionally grouped with actor $strParent using unknown option $strOption")
                            }
                        }
                    }
                }
            }
        }
    }

    private  fun optionExists(strOptionId: String): Boolean {
        for(profile in profiles.values) {
            if (profile.findOption(strOptionId) != null) {
                return true;
            }
        }

        return false;
    }


    // Gather all object identifiers and make sure they are unique.
    private fun validateObjectIds() {
        val allObjectIds = mutableMapOf<String,String>()

        for(profile in profiles.values) {
            checkAndAddObjectIds(profile.oids, profile.profileId, allObjectIds)

            for(profileOption in profile.options) {
                checkAndAddObjectIds(profileOption.oids, profileOption.id, allObjectIds)
            }

            for(actor in profile.actorReferences()) {
                checkAndAddObjectIds(actor.oids, actor.id, allObjectIds)
            }
        }

        for(transaction in transactions.values) {
            checkAndAddObjectIds(transaction.oids, transaction.id, allObjectIds)
        }

        for(contentModule in contentModules.values) {
            checkAndAddObjectIds(contentModule.oids, contentModule.id, allObjectIds)
        }

        for(useCase in useCases.values) {
            checkAndAddObjectIds(useCase.oids, useCase.id, allObjectIds)

            for(scenario in useCase.specification.scenarios) {
                checkAndAddObjectIds(scenario.oids, scenario.title, allObjectIds)
            }
        }

        for(req in requirements.values) {
            checkAndAddOid(req.oid, req.localId, allObjectIds)
        }
    }

    private fun checkAndAddObjectIds(objectIds : List<String>, strContext: String, allObjectOids: MutableMap<String, String>) {
        for(strOid in objectIds) {
            checkAndAddOid(strOid, strContext, allObjectOids)
        }
    }
    private fun checkAndAddOid(strOid: String, strContext: String, allObjectIds: MutableMap<String,String>) {
        check(!allObjectIds.containsKey(strOid)) {
            logger.error("$strContext has duplicate object id `$strOid`. First was ${allObjectIds[strOid]}")
        }
        allObjectIds[strOid] = strContext
    }


    //endregion

}