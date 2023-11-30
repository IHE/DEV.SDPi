# Changelog

All notable changes to the SDPi supplement will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to the following versioning scheme describe in the [SDPi wiki](https://github.com/IHE/DEV.SDPi/wiki/SDPi-Editorial-Planning-and-Versions#major--minor-versioning).

## Modifications to the Keep a Changelog format

The SDPi changelog shall not contain the following sections
   - Deprecated
   - Security

Each section shall contain a list of action items of the following format: `<brief one-sentence description of what has been done> (#<issue number & URL>).`

## [Unreleased]

### Added

- Requirements R0019 and R0020 to prohibit the use of QNames and XML mixed content ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Requirements R1012 and R1013 to process QNames in BICEPS and DPWS ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Scope of Application section added to TF-1 SDPi Overview ([#235](https://github.com/IHE/DEV.SDPi/issues/235))
- Update Title Page to Better Reflect HL7 Aspects of the SDPi Standard ([#240](https://github.com/IHE/DEV.SDPi/issues/240))
- Added R0702 on private coding system identification in volume 3 and updated private mdc code mapping in volume 2 gateways ([#57](https://github.com/IHE/DEV.SDPi/issues/57)).

### Changed

### Removed

- _OBX-17 field is not left empty_ as this would be interpreted as derivation method _auto_ ([#233](https://github.com/IHE/DEV.SDPi/issues/233)).

### Editorial Fixes

- Moved reference content in glossary table between columns ([#166](https://github.com/IHE/DEV.SDPi/issues/166)).

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
- Created a common section for the ACM and DEC gateways such that requirement numbers are not duplicated anymore ([#53](https://github.com/IHE/DEV.SDPi/issues/53).  
- Rephrase R1542 to make sure, that system functions not being available don't lead to unnecessary alarms ([#180] (https://github.com/IHE/DEV.SDPi/issues/180)). 
- Moved requirements R1542 & R1543 to the SDPi-P SES / Effectiveness section from Github issues ([#182](https://github.com/IHE/DEV.SDPi/issues/182)).

### Removed

### Editorial Fixes

- Wrong appendix numbering in references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Missing volume number in section references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Updated all references to the sdpi-fhir Github ([#162](https://github.com/IHE/DEV.SDPi/issues/162))
- Addressed editorial issues from the IHE Publications review ([#189](https://github.com/IHE/DEV.SDPi/issues/189))
- Wrong appendix numbering in references ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- All instances of "i.e." or "e.g." replaced with "i.e.," and "e.g.,", respectivelly ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "profile" to "Profile" where appropriate ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "section" to "Section" when it was referring to a specific clause within the document ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).

## [1.0.1] - 2023-04-14

### Editorial Fixes

- Wrong appendix numbering offset in tables to be aligned with appendix letter ([#134](https://github.com/IHE/DEV.SDPi/issues/134)).
- Path to XML file to correctly render Figure 3:8.7.3.17-1 Physiologic Monitor Containment Tree Example ([#136](https://github.com/IHE/DEV.SDPi/issues/136)).
- Corrected the PUML file name in TF-1:12.1.1-1 SDPi-A to render the alert reporting sequence diagram ([#135](https://github.com/IHE/DEV.SDPi/issues/135)).
- Replaced high-lighting from "Closed Issues" section with correct list ([#139](https://github.com/IHE/DEV.SDPi/issues/139)).
- Remove leading dashes from table and figure captions if the section number is empty ([#144](https://github.com/IHE/DEV.SDPi/issues/144)).
- Reviewed all "Verson Note" and version-specific (i.e, 1.0) content and either reworded or removed ([#148](https://github.com/IHE/DEV.SDPi/issues/148))

# Changelog

All notable changes to the SDPi supplement will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to the following versioning scheme describe in the [SDPi wiki](https://github.com/IHE/DEV.SDPi/wiki/SDPi-Editorial-Planning-and-Versions#major--minor-versioning).

## Modifications to the Keep a Changelog format

The SDPi changelog shall not contain the following sections
   - Deprecated
   - Security

Each section shall contain a list of action items of the following format: `<brief one-sentence description of what has been done> (#<issue number & URL>).`

## [Unreleased]

### Added

- Requirements R0019 and R0020 to prohibit the use of QNames and XML mixed content ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Requirements R1012 and R1013 to process QNames in BICEPS and DPWS ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Added R0702 on private coding system identification in volume 3 and updated private mdc code mapping in volume 2 gateways ([#57](https://github.com/IHE/DEV.SDPi/issues/57)).

### Changed

### Removed

### Editorial Fixes

- Moved reference content in glossary table between columns ([#166](https://github.com/IHE/DEV.SDPi/issues/166))


## [1.1.0] - 2023-07-21

### Added

- Process requirement R1500 for consideration of clock misalignment ([#154](https://github.com/IHE/DEV.SDPi/issues/154)).
- Use cases _Devices are operational in the MD LAN network but cannot access the TS Service_ and _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#155](https://github.com/IHE/DEV.SDPi/issues/155)). 
- MDIB Efficiency clause ([#50](https://github.com/IHE/DEV.SDPi/issues/50)).
- Requirement explicitly forbidding manual TS service configuration ([#30](https://github.com/IHE/DEV.SDPi/issues/30))
- Added safety, security and effectiveness requirements to use case _Devices are operational in the MD LAN network but cannot access the TS Service_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Added safety, security and effectiveness requirements for use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Labeling requirement to use case _Device is connected to the MD LAN network with a Time Source service_ to specify NTP configuration ([#151](https://github.com/IHE/DEV.SDPi/issues/151)). 


### Changed

- Use case _Device is connected to the MD LAN network and a user wants to change the device's time_ to account for the fact, that configuring the TS service manually is always forbidden, not just when TS service is operational ([#30](https://github.com/IHE/DEV.SDPi/issues/30)).
- Changed use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ so that the decision to continue/discontinue the execution of a System Function while the clocks become less accurate lies with the consumer ([#31](https://github.com/IHE/DEV.SDPi/issues/31)). 
- Open/Closed Issues sections have been revised. Sections are now generated automatically from Github issues ([#20](https://github.com/IHE/DEV.SDPi/issues/20)).
- Created a common section for the ACM and DEC gateways such that requirement numbers are not duplicated anymore ([#53](https://github.com/IHE/DEV.SDPi/issues/53).  
- Rephrase R1542 to make sure, that system functions not being available don't lead to unnecessary alarms ([#180] (https://github.com/IHE/DEV.SDPi/issues/180)). 
- Moved requirements R1542 & R1543 to the SDPi-P SES / Effectiveness section from Github issues ([#182](https://github.com/IHE/DEV.SDPi/issues/182)).

### Removed

### Editorial Fixes

- Wrong appendix numbering in references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Missing volume number in section references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Updated all references to the sdpi-fhir Github ([#162](https://github.com/IHE/DEV.SDPi/issues/162))
- Addressed editorial issues from the IHE Publications review ([#189](https://github.com/IHE/DEV.SDPi/issues/189))
- Wrong appendix numbering in references ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- All instances of "i.e." or "e.g." replaced with "i.e.," and "e.g.,", respectivelly ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "profile" to "Profile" where appropriate ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "section" to "Section" when it was referring to a specific clause within the document ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).

## [1.0.1] - 2023-04-14

### Editorial Fixes

- Wrong appendix numbering offset in tables to be aligned with appendix letter ([#134](https://github.com/IHE/DEV.SDPi/issues/134)).
- Path to XML file to correctly render Figure 3:8.7.3.17-1 Physiologic Monitor Containment Tree Example ([#136](https://github.com/IHE/DEV.SDPi/issues/136)).
- Corrected the PUML file name in TF-1:12.1.1-1 SDPi-A to render the alert reporting sequence diagram ([#135](https://github.com/IHE/DEV.SDPi/issues/135)).
- Replaced high-lighting from "Closed Issues" section with correct list ([#139](https://github.com/IHE/DEV.SDPi/issues/139)).
- Remove leading dashes from table and figure captions if the section number is empty ([#144](https://github.com/IHE/DEV.SDPi/issues/144)).
- Reviewed all "Verson Note" and version-specific (i.e, 1.0) content and either reworded or removed ([#148](https://github.com/IHE/DEV.SDPi/issues/148))
# Changelog

All notable changes to the SDPi supplement will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to the following versioning scheme describe in the [SDPi wiki](https://github.com/IHE/DEV.SDPi/wiki/SDPi-Editorial-Planning-and-Versions#major--minor-versioning).

## Modifications to the Keep a Changelog format

The SDPi changelog shall not contain the following sections
   - Deprecated
   - Security

Each section shall contain a list of action items of the following format: `<brief one-sentence description of what has been done> (#<issue number & URL>).`

## [Unreleased]

### Added

- Requirements R0019 and R0020 to prohibit the use of QNames and XML mixed content ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Requirements R1012 and R1013 to process QNames in BICEPS and DPWS ([#210](https://github.com/IHE/DEV.SDPi/issues/210)).
- Added R0702 on private coding system identification in volume 3 and updated private mdc code mapping in volume 2 gateways ([#57](https://github.com/IHE/DEV.SDPi/issues/57)).

### Changed

### Removed

### Editorial Fixes

- Moved reference content in glossary table between columns ([#166](https://github.com/IHE/DEV.SDPi/issues/166))


## [1.1.0] - 2023-07-21

### Added

- Process requirement R1500 for consideration of clock misalignment ([#154](https://github.com/IHE/DEV.SDPi/issues/154)).
- Use cases _Devices are operational in the MD LAN network but cannot access the TS Service_ and _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#155](https://github.com/IHE/DEV.SDPi/issues/155)). 
- MDIB Efficiency clause ([#50](https://github.com/IHE/DEV.SDPi/issues/50)).
- Requirement explicitly forbidding manual TS service configuration ([#30](https://github.com/IHE/DEV.SDPi/issues/30))
- Added safety, security and effectiveness requirements to use case _Devices are operational in the MD LAN network but cannot access the TS Service_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Added safety, security and effectiveness requirements for use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Labeling requirement to use case _Device is connected to the MD LAN network with a Time Source service_ to specify NTP configuration ([#151](https://github.com/IHE/DEV.SDPi/issues/151)). 


### Changed

- Use case _Device is connected to the MD LAN network and a user wants to change the device's time_ to account for the fact, that configuring the TS service manually is always forbidden, not just when TS service is operational ([#30](https://github.com/IHE/DEV.SDPi/issues/30)).
- Changed use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ so that the decision to continue/discontinue the execution of a System Function while the clocks become less accurate lies with the consumer ([#31](https://github.com/IHE/DEV.SDPi/issues/31)). 
- Open/Closed Issues sections have been revised. Sections are now generated automatically from Github issues ([#20](https://github.com/IHE/DEV.SDPi/issues/20)).
- Created a common section for the ACM and DEC gateways such that requirement numbers are not duplicated anymore ([#53](https://github.com/IHE/DEV.SDPi/issues/53).  
- Rephrase R1542 to make sure, that system functions not being available don't lead to unnecessary alarms ([#180] (https://github.com/IHE/DEV.SDPi/issues/180)). 
- Moved requirements R1542 & R1543 to the SDPi-P SES / Effectiveness section from Github issues ([#182](https://github.com/IHE/DEV.SDPi/issues/182)).

### Removed

### Editorial Fixes

- Wrong appendix numbering in references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Missing volume number in section references ([#189](https://github.com/IHE/DEV.SDPi/issues/189)).
- Updated all references to the sdpi-fhir Github ([#162](https://github.com/IHE/DEV.SDPi/issues/162))
- Addressed editorial issues from the IHE Publications review ([#189](https://github.com/IHE/DEV.SDPi/issues/189))
- Wrong appendix numbering in references ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- All instances of "i.e." or "e.g." replaced with "i.e.," and "e.g.,", respectivelly ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "profile" to "Profile" where appropriate ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).
- Normalized "section" to "Section" when it was referring to a specific clause within the document ([#189](https://github.com/IHE/DEV.SDPi/issues/189))).

## [1.0.1] - 2023-04-14

### Editorial Fixes

- Wrong appendix numbering offset in tables to be aligned with appendix letter ([#134](https://github.com/IHE/DEV.SDPi/issues/134)).
- Path to XML file to correctly render Figure 3:8.7.3.17-1 Physiologic Monitor Containment Tree Example ([#136](https://github.com/IHE/DEV.SDPi/issues/136)).
- Corrected the PUML file name in TF-1:12.1.1-1 SDPi-A to render the alert reporting sequence diagram ([#135](https://github.com/IHE/DEV.SDPi/issues/135)).
- Replaced high-lighting from "Closed Issues" section with correct list ([#139](https://github.com/IHE/DEV.SDPi/issues/139)).
- Remove leading dashes from table and figure captions if the section number is empty ([#144](https://github.com/IHE/DEV.SDPi/issues/144)).
- Reviewed all "Verson Note" and version-specific (i.e, 1.0) content and either reworded or removed ([#148](https://github.com/IHE/DEV.SDPi/issues/148))
