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

- Process requirement R1500 for consideration of clock misalignment ([#154](https://github.com/IHE/DEV.SDPi/issues/154)).
- Use cases _Devices are operational in the MD LAN network but cannot access the TS Service_ and _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#155](https://github.com/IHE/DEV.SDPi/issues/155)). 
- MDIB Efficiency clause ([#50](https://github.com/IHE/DEV.SDPi/issues/50)).
- Requirement explicitly forbidding manual TS service configuration ([#30](https://github.com/IHE/DEV.SDPi/issues/30))
- Added safety, security and effectiveness requirements to use case _Devices are operational in the MD LAN network but cannot access the TS Service_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Added safety, security and effectiveness requirements for use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ ([#31](https://github.com/IHE/DEV.SDPi/issues/31)).
- Labeling requirement to use case _Device is connected to the MD LAN network with a Time Source service_ to specify NTP configuration ([#151](https://github.com/IHE/DEV.SDPi/issues/151)). 


### Changed

- Use case _Device is connected to the MD LAN network and a user wants to change the device's time_ to account for the fact, that configuring the TS service manually is always forbidden, not just when TS service is operational. ([#30](https://github.com/IHE/DEV.SDPi/issues/30))
- Changed use case _Devices are operational in the MD LAN network but cannot access the TS Service and clock drift is unacceptable_ so that the decision to continue/discontinue the execution of a System Function while the clocks become less accurate lies with the consumer. 


### Removed

### Editorial Fixes

## [1.0.1] - 2023-04-14

### Editorial Fixes

- Wrong appendix numbering offset in tables to be aligned with appendix letter ([#134](https://github.com/IHE/DEV.SDPi/issues/134)).
- Path to XML file to correctly render Figure 3:8.7.3.17-1 Physiologic Monitor Containment Tree Example ([#136](https://github.com/IHE/DEV.SDPi/issues/136)).
- Corrected the PUML file name in TF-1:12.1.1-1 SDPi-A to render the alert reporting sequence diagram ([#135](https://github.com/IHE/DEV.SDPi/issues/135)).
- Replaced high-lighting from "Closed Issues" section with correct list ([#139](https://github.com/IHE/DEV.SDPi/issues/139)).
- Remove leading dashes from table and figure captions if the section number is empty ([#144](https://github.com/IHE/DEV.SDPi/issues/144)).