# Changelog

All notable changes to the SDPi supplement will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to the following versioning scheme describe in the [SDPi wiki](https://github.com/IHE/DEV.SDPi/wiki/SDPi-Editorial-Planning-and-Versions#major--minor-versioning).

## Modifications to the Keep a Changelog format

The SDPi changelog shall not contain the following sections
 
- Deprecated
- Security

Each section shall contain a list of action items of the following format: `<brief one-sentence description of what has been done> (#<issue number & URL>).`

## Unreleased

### Added

- sdpi:ReportSequence extension to separate message sequencing and versioning ([#321](https://github.com/IHE/DEV.SDPi/issues/321)).
- Semantic markup for profiles, profile-options, requirements, gateways, use-cases to support rich artefact generation ([#463, part 2](https://github.com/IHE/DEV.SDPi/issues/463))
- OID summary table for profiles, actors, transactions, content modules, use cases, requirements ([#449](https://github.com/IHE/DEV.SDPi/issues/449))
- Implementation conformity statement tables ([#463, part 2](https://github.com/IHE/DEV.SDPi/issues/463))
- Relations extension to model inter-MDS relationships ([#48](https://github.com/IHE/DEV.SDPi/issues/48))

### Changes

- Updated documentation for assigning unique identifiers ([#449](https://github.com/IHE/DEV.SDPi/issues/449))
- Split Gateway Direction Option into Gateway Export Direction Option and Gateway Import Direction Option ([#463, part 2](https://github.com/IHE/DEV.SDPi/issues/463))
- Corrected actor (option) references in sections 1:12.1 and 1:13.1 ([#413](https://github.com/IHE/DEV.SDPi/issues/413))
- Improved SDPi profiles diagram in Volume 1 ([#285](https://github.com/IHE/DEV.SDPi/issues/285))

### Editorial Fixes
- Ported missed changes of release 2.2.1 back into the 2.3 series ([#489](https://github.com/IHE/DEV.SDPi/issues/489))

## [2.2.1] - 2025-11-07

### Editorial Fixes

- IHE Publications change for SDPi 2.2 publication (one footnote rephrased) ([#488](https://github.com/IHE/DEV.SDPi/issues/488))

## [2.2.0] - 2025-10-02

### Added

- DEV-33 transaction (localization service), including MDPWS binding. ([#40](https://github.com/IHE/DEV.SDPi/issues/40))
- FHIR Gateway to the SDPi-R Actors table ([#388](https://github.com/IHE/DEV.SDPi/issues/388))
- Section "Numeric Metric Display Precision" to volume 3 ([#392](https://github.com/IHE/DEV.SDPi/issues/392))
- SDPi-P SDC Gateway Direction profile option ([#396](https://github.com/IHE/DEV.SDPi/issues/396))

### Changes

- Removed withdrawn transaction DEV-34 (Announce Network Departure) and updated references to future SDPi versions for maintainability ([#406](https://github.com/IHE/DEV.SDPi/issues/406))
- Profile Required Actor Groupings - Update with Table ([#387](https://github.com/IHE/DEV.SDPi/issues/387))
- Corrected erroneous actor names in transactions DEV-35 through DEV-40 ([#461](https://github.com/IHE/DEV.SDPi/issues/461))
- Corrected erroneous actor name in Table 1:12.1-1 note ([#464](https://github.com/IHE/DEV.SDPi/issues/464))
- Changed all references to IHE PCD TF 2019 to the newer 2024 version. Updated sections in document which were affected by the TF version change ([#340](https://github.com/IHE/DEV.SDPi/issues/340))

### Editorial Fixes

- Rephrased for clarity the first bullet of section 1:A.1 ([#316](https://github.com/IHE/DEV.SDPi/issues/316))
- Fixed malformed references to appendices ([#453](https://github.com/IHE/DEV.SDPi/issues/453))
- Fixed broken references to PlantUML files ([#472](https://github.com/IHE/DEV.SDPi/issues/472))
- Removed cover page statement about PDF version availability ([#247](https://github.com/IHE/DEV.SDPi/issues/247))
- Updated Supplement Notes ([#462](https://github.com/IHE/DEV.SDPi/issues/462))

## [2.1.2] - 2025-05-09

### Editorial Fixes

- Minor typos and malformed references caused by erroneous Asciidoc conversion.

## [2.1.1] - 2025-04-22

### Editorial Fixes

- Malformed PlantUML rendering and titles in section 2:A.2.6 ([#441](https://github.com/IHE/DEV.SDPi/issues/441))

## [2.1.0] - 2025-04-22

### Changes

- Added new section "Unique Localized Text Catalog Identification" to volume 3 ([#256](https://github.com/IHE/DEV.SDPi/issues/256))
- Removed "and ignore" from BICEPS Content Consumer requirement ([#380](https://github.com/IHE/DEV.SDPi/issues/380))
- Rephrased notes, mostly to correct and maintain references to SDPi versions ([#366](https://github.com/IHE/DEV.SDPi/issues/366))
- Corrected normative standard reference in transactions DEV-27, DEV-35 and DEV-38 ([#400](https://github.com/IHE/DEV.SDPi/issues/400))
- Updated SDPi Issue Management section ([#408](https://github.com/IHE/DEV.SDPi/issues/408))
- Rephrased paragraph describing the SOMDS Smart App Platform Actor for clarity ([#379](https://github.com/IHE/DEV.SDPi/issues/379))
- Modified transaction DEV-29 to add DescriptionModification and EpisodicOperationalState reports ([#403](https://github.com/IHE/DEV.SDPi/issues/403))

### Editorial Fixes

- Corrected typos and grammar ([#363](https://github.com/IHE/DEV.SDPi/issues/363))
- Changed order of subsections in 1:11.1, 1:12.1 and 1:13.1 ([365](https://github.com/IHE/DEV.SDPi/issues/365))

### Added

- Content on how to deal with non-slewing time adjustments ([#203](https://github.com/IHE/DEV.SDPi/issues/203))
- TF-2C for Security Management Appendix (basic, with little initial content)  ([#250](https://github.com/IHE/DEV.SDPi/issues/250))
- Clarification in definition of SOMDS Provider UID ([#378](https://github.com/IHE/DEV.SDPi/issues/378))
- Further reasons for security technology variation ([#381](https://github.com/IHE/DEV.SDPi/issues/381))
- Clarification of rationale for discontinuous section numbering ([#364](https://github.com/IHE/DEV.SDPi/issues/364))
- Clarification of status of open Issues and Topics of Interest ([#374](https://github.com/IHE/DEV.SDPi/issues/374))
- Clarification of TLS 1.2 versus TLS 1.3 usage ([#377](https://github.com/IHE/DEV.SDPi/issues/377) and [#423](https://github.com/IHE/DEV.SDPi/issues/423))
- Clarification of TF-X acronym meaning ([#376](https://github.com/IHE/DEV.SDPi/issues/376))
- Descriptive overview of SDC gateways ([#382](https://github.com/IHE/DEV.SDPi/issues/382))
- Clarification of external control meaning ([#383](https://github.com/IHE/DEV.SDPi/issues/383))
- Clarification of technical confirmation versus operator confirmation ([#371](https://github.com/IHE/DEV.SDPi/issues/371))
- Clarification of security versus cybersecurity ([#370](https://github.com/IHE/DEV.SDPi/issues/370))
- Clarification on ACNS 5.1 - Added SES discussion for SDPi-A risk management section ([#368](https://github.com/IHE/DEV.SDPi/issues/368))

## [2.0.0] - 2024-12-13

### Changes

- Fixed action URN in waveform stream example ([#292](https://github.com/IHE/DEV.SDPi/issues/292))
- Clarified intentionality of difference between ad-hoc and managed discovery ([#317](https://github.com/IHE/DEV.SDPi/issues/317))
- Clarified FHIR Gateway support for multiple paradigms ([#264](https://github.com/IHE/DEV.SDPi/issues/264))
- Clarified that the SES sections are not comprehensive for risk management ([#271](https://github.com/IHE/DEV.SDPi/issues/271))
- Bibliography note added indicating the rationale for the subcategories ([#266](https://github.com/IHE/DEV.SDPi/issues/266))
- SDPi Use Case Approach explanatory text added to start of supplement ([#263](https://github.com/IHE/DEV.SDPi/issues/263))

- Enhanced explanation of SOA aspects of the SDC/SDPi specification (HL7 Ballot) ([#276](https://github.com/IHE/DEV.SDPi/issues/276))

### Editorial Fixes

- Added deprecation notice for three requirements due to expected impact of BICEPS standard corrigendum ([#334](https://github.com/IHE/DEV.SDPi/issues/334))
- Updated SDPi "Topic of Interest" issue management for clarity ([#274](https://github.com/IHE/DEV.SDPi/issues/274))
- Addressed three JIRA tickets listed in issue ([#275](https://github.com/IHE/DEV.SDPi/issues/275))
- Corrected some entries in "1:B.1 Referenced Standards" for consistency ([#267](https://github.com/IHE/DEV.SDPi/issues/267))
- Commented out concept sections 1:10.4.1.3 thru 1:10.4.1.10 and minimized referencing to years for improving readability ([#268](https://github.com/IHE/DEV.SDPi/issues/268))
- Removed TBD statements from Table A-1 ([#273](https://github.com/IHE/DEV.SDPi/issues/273))
- Updated per IHE Style Conventions for numbers-in-text, em dashes, punctuation marks, etc. ([#269](https://github.com/IHE/DEV.SDPi/issues/269))

### Added

- Added SDPi FHIR Gateway ([#252](https://github.com/IHE/DEV.SDPi/issues/252))

## [1.4.1] - 2024-10-04

### Editorial Fixes

- IHE Publications Editor review corrections - typos and style corrections ([#319](https://github.com/IHE/DEV.SDPi/pull/319))
- Unclarities and typos in TF-1:A.1 ([#316](https://github.com/IHE/DEV.SDPi/issues/316))

### Added

- No additions

## [1.4.0] - 2024-09-17

### Editorial Fixes

- Correct SES section titles ([#272](https://github.com/IHE/DEV.SDPi/issues/272))
- Implement semi-automatic version referencing by adding Asciidoc variables ( ([#284](https://github.com/IHE/DEV.SDPi/issues/284)))
- Extended Requirements Interoperability (RI) specification, mostly in TF-1A ( ([#299](https://github.com/IHE/DEV.SDPi/issues/299))); note: issue subtasks not completed in R1.4 have been pushed into ([315](https://github.com/IHE/DEV.SDPi/issues/315)).
- HL7 2024-May: Alert Requirements still under development ( ([294](https://github.com/IHE/DEV.SDPi/issues/294)))

### Added

- Add discovery proxy transactions scaffold and actor description ( ([152](https://github.com/IHE/DEV.SDPi/issues/152)))

## [1.3.1] - 2024-04-05

### Editorial Fixes

- Remove / Update "1.2" comments from release 1.3.0 ([#282](https://github.com/IHE/DEV.SDPi/issues/282))

## [1.3.0] - 2024-03-29

### Added

- Message mapping for DEV-39 ([#251](https://github.com/IHE/DEV.SDPi/issues/251))
- Requirement to enforce serialization of any notification ([#259](https://github.com/IHE/DEV.SDPi/issues/259))
- Info regarding correctness of message excerpts ([#260](https://github.com/IHE/DEV.SDPi/issues/260))
- Modelling and requirements of compound metrics ([#228](https://github.com/IHE/DEV.SDPi/issues/228))

### Editorial Fixes

- Corrected the CDAS section title to correctly reflect the meaning of the acronym ([#265](https://github.com/IHE/DEV.SDPi/issues/265))
- Reformatted the SDPi profiles scope section for readability ([270](https://github.com/IHE/DEV.SDPi/issues/270))

## [1.2.0] - 2023-12-07

### Added

- Requirements R0019 and R0020 to prohibit the use of QNames and XML mixed content ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Requirements R1012 and R1013 to process QNames in BICEPS and DPWS ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Scope of Application section added to TF-1 SDPi Overview ([#235](https://github.com/IHE/DEV.SDPi/issues/235))
- Update Title Page to Better Reflect HL7 Aspects of the SDPi Standard ([#240](https://github.com/IHE/DEV.SDPi/issues/240))
- Added R0702 on private coding system identification in volume 3 and updated private mdc code mapping in volume 2 gateways ([#57](https://github.com/IHE/DEV.SDPi/issues/57)).

### Removed

- _OBX-17 field is not left empty_ as this would be interpreted as derivation method _auto_ ([#233](https://github.com/IHE/DEV.SDPi/issues/233)).

### Editorial Fixes

- Moved reference content in glossary table between columns ([#166](https://github.com/IHE/DEV.SDPi/issues/166)).
- Apply several editorial fixes ([#247](https://github.com/IHE/DEV.SDPi/issues/247))

## [1.1.0] - 2023-07-21

### Added

- Process requirement R1500 for consideration of clock misalignment ([#154](https://github.com/IHE/DEV.SDPi/issues/154)).
- Use cases _Devices are operational in the MD LAN network but cannot access the TS Service_ and _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#155](https://github.com/IHE/DEV.SDPi/issues/155)). 
- MDIB Efficiency clause ([#50](https://github.com/IHE/DEV.SDPi/issues/50)).
- Requirement explicitly forbidding manual TS service configuration ([#30](https://github.com/IHE/DEV.SDPi/issues/30))
- Added safety, security and effectiveness requirements to use case _Devices are operational in the MD LAN network but cannot access the TS Service_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Added safety, security and effectiveness requirements for use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Labeling requirement to use case _Device is connected to the MD LAN network with a Time Source service_ to specify NTP configuration ([#151](https://github.com/IHE/DEV.SDPi/issues/151)). 
- Added two requirements: One for the accompanying documentation to make sure that participants synchronize with the same time-server and one that explicitly forbids executing system functions without a TS service, when a device is connected to the network. ([#202](https://github.com/IHE/DEV.SDPi/issues/202))

### Changed

- Use case _Device is connected to the MD LAN network and a user wants to change the device's time_ to account for the fact, that configuring the TS service manually is always forbidden, not just when TS service is operational ([#30](https://github.com/IHE/DEV.SDPi/issues/30)).
- Changed use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ so that the decision to continue/discontinue the execution of a System Function while the clocks become less accurate lies with the consumer ([#31](https://github.com/IHE/DEV.SDPi/issues/31)). 
- Open/Closed Issues sections have been revised. Sections are now generated automatically from Github issues ([#20](https://github.com/IHE/DEV.SDPi/issues/20)).
- Created a common section for the ACM and DEC gateways such that requirement numbers are not duplicated anymore ([#53](https://github.com/IHE/DEV.SDPi/issues/53)).  
- Rephrase R1542 to make sure, that system functions not being available don't lead to unnecessary alarms ([#180] (https://github.com/IHE/DEV.SDPi/issues/180)). 
- Moved requirements R1542 & R1543 to the SDPi-P SES / Effectiveness section from Github issues ([#182](https://github.com/IHE/DEV.SDPi/issues/182)).

### Removed

### Editorial Fixes

- Wrong appendix numbering in references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Missing volume number in section references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Updated all references to the sdpi-fhir Github ([#162](https://github.com/IHE/DEV.SDPi/issues/162))
- Addressed editorial issues from the IHE Publications review ([#189](https://github.com/IHE/DEV.SDPi/issues/189))
- All instances of "i.e." or "e.g." replaced with "i.e.," and "e.g.,", respectivelly ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "profile" to "Profile" where appropriate ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "section" to "Section" when it was referring to a specific clause within the document ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).

## [1.0.1] - 2023-04-14

### Editorial Fixes

- Wrong appendix numbering offset in tables to be aligned with appendix letter ([#134](https://github.com/IHE/DEV.SDPi/issues/134)).
- Path to XML file to correctly render Figure 3:8.7.3.17-1 Physiologic Monitor Containment Tree Example ([#136](https://github.com/IHE/DEV.SDPi/issues/136)).
- Corrected the PUML file name in TF-1:12.1.1-1 SDPi-A to render the alert reporting sequence diagram ([#135](https://github.com/IHE/DEV.SDPi/issues/135)).
- Replaced high-lighting from "Closed Issues" section with correct list ([#139](https://github.com/IHE/DEV.SDPi/issues/139)).
- Remove leading dashes from table and figure captions if the section number is empty ([#144](https://github.com/IHE/DEV.SDPi/issues/144)).
- Reviewed all "Verson Note" and version-specific (i.e, 1.0) content and either reworded or removed ([#148](https://github.com/IHE/DEV.SDPi/issues/148))

