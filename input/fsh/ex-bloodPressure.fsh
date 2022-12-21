Instance:   ex-IHE-FooBar-bloodPressure-0
InstanceOf: IHE.FooBar.bloodPressure
Title: "Example of an IHE FooBar blood pressure R4 observation, minimal"
Description:      "holding typical values"
* meta.security = http://terminology.hl7.org/CodeSystem/v3-ActReason#HTEST
* status = #final
* category = http://terminology.hl7.org/CodeSystem/observation-category#vital-signs "Vital Signs"
* code.coding[loincCode1] = LOINC#55284-4 "Blood pressure systolic and diastolic"
* code.coding[loincCode2] = LOINC#8716-3 "Vital Signs"
* subject = Reference(Patient/ex-patient)
* effectiveDateTime = 2004-12-25T23:50:50-05:00
* component[systolicBP].code = LOINC#8480-6 "Systolic blood pressure"
* component[systolicBP].valueQuantity = 140.0 'mm[Hg]'
* component[diastolicBP].code = LOINC#8462-4 "Diastolic blood pressure"
* component[diastolicBP].valueQuantity = 90.0 'mm[Hg]'
* note.text = "a minimal blood pressure"