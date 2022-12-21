Profile:        FooBarbloodPressure
Parent:         Observation
Id:             IHE.FooBar.bloodPressure
Title:          "IHE FooBar Blood Pressure Observation"
Description:    """
A profile on the Observation that declares how FooBar will Create/Update blood pressure measurements.
* must have vital-signs category
* must have LOINC#55284-4 code AND LOINC#8716-3
* must have effectiveDateTime
* must not have a value[x]
* must have two components
* must have systolic and diastolic component values in mm[Hg]
* may have a heart-rate component
* may have a position component
* must have status at final
* must point at the patient
* may have a note (comment)
* once created will or might have an id, versionId, lastUpdated, text, and identifier
"""
* ^version = "0.1.0"
* ^date = "2020-11-23"
* category 1..1
* category = http://terminology.hl7.org/CodeSystem/observation-category#vital-signs
// This shows how to slice to force two values
* code.coding ^slicing.discriminator.type = #value
* code.coding ^slicing.discriminator.path = "$this"
* code.coding ^slicing.rules = #closed
* code.coding 2..2
* code.coding contains loincCode1 1..1 and loincCode2 1..1
* code.coding[loincCode1] = LOINC#55284-4
* code.coding[loincCode2] = LOINC#8716-3
* effectiveDateTime 1..1
* value[x] 0..0
* component 2..4
// This shows slicing on code within component
* component ^slicing.discriminator.type = #pattern
* component ^slicing.discriminator.path = "code"
* component ^slicing.rules = #closed
* component ^slicing.description = "blood pressure values"
* component contains 
	diastolicBP 1..1 and 
	systolicBP 1..1 and 
	heartRate 0..1 and 
	bodyContextSitting 0..1 and 
	bodyContextStanding 0..1 and 
	bodyContextSupine 0..1 and 
	bodyContextResting 0..1
* component[systolicBP].code = LOINC#8480-6 // Systolic blood pressure
* component[systolicBP].value[x] only Quantity
* component[systolicBP].valueQuantity = UCUM#mm[Hg] // "mmHg"
* component[diastolicBP].code = LOINC#8462-4 // Diastolic blood pressure
* component[diastolicBP].value[x] only Quantity
* component[diastolicBP].valueQuantity = UCUM#mm[Hg] // "mmHg"
* component[heartRate].code = LOINC#8867-4 // heart rate
* component[heartRate].value[x] only Quantity
* component[heartRate].valueQuantity = UCUM#/min "beats/minute"
// This is odd, but showing it for sushi learning - TODO should leverage the valueset below
* component[bodyContextSitting].code = LOINC#69000-8
* component[bodyContextSitting].value[x] only string
* component[bodyContextSitting].valueString = "Sitting"
* component[bodyContextStanding].code = LOINC#69001-6
* component[bodyContextStanding].value[x] only string
* component[bodyContextStanding].valueString = "Standing"
* component[bodyContextSupine].code = LOINC#68999-2
* component[bodyContextSupine].value[x] only string
* component[bodyContextSupine].valueString = "Supine"
* component[bodyContextResting].code = LOINC#40443-4
* component[bodyContextResting].value[x] only string
* component[bodyContextResting].valueString = "Resting"
* status = #final
* subject 1..1
* subject only Reference(Patient)
* note 0..1
// things that are not declared in the mapping table but likely are populated because they are normal REST processing
//* id 0..1
//* meta.versionId 0..1
//* meta.lastUpdated 0..1
//* meta.profile 0..1
//* meta.source 0..0
//* meta.security 0..0
//* identifier 0..*
//* text 0..0
// this is making everything else in observation forbidden. If they show up, apps should not misbehave. IHE FooBar compliant apps shall not populate these in a Create or Update.
* implicitRules 0..0
* language 0..0
* contained 0..0
* extension 0..0
* modifierExtension 0..0
* basedOn 0..0
* partOf 0..0
* focus 0..0
* encounter 0..0
* issued 0..0
* performer 0..0
* dataAbsentReason 0..0
* interpretation 0..0
* bodySite 0..0
* method 0..0
* specimen 0..0
* device 0..0
* referenceRange 0..0
* hasMember 0..0
* derivedFrom 0..0

// This is showing how to build a valueSet. This will be a standalone valueset, but is built here because this is where it is first used
ValueSet: IheFooBarBloodPressurePositions
Id: ihe-foobar-blood-pressure-positions
Title: "The methods of measurement for blood pressure"
Description: "These are the loinc body positions that IHE FooBar supports on blood pressure."
* ^experimental = false
* LOINC#69000-8 "Heart rate - sitting"
* LOINC#69001-6 "Heart rate - standing"
* LOINC#68999-2 "Heart rate - supine"
* LOINC#40443-4 "Heart rate - resting"
