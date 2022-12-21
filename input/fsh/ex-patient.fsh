Instance:   ex-patient
InstanceOf: Patient
Title: "Example Patient"
Description: "The patient from which all the example relate"
// history - http://playgroundjungle.com/2018/02/origins-of-john-jacob-jingleheimer-schmidt.html
* meta.security = http://terminology.hl7.org/CodeSystem/v3-ActReason#HTEST
* name[0].use = #usual
* name[0].family = "Smith"
* name[0].given = "Jack"
* name[1].use = #old
* name[1].family = "Schnidt"
* name[1].given[0] = "John"
* name[1].given[1] = "Jacob"
* name[1].given[2] = "Jingle"
* name[1].given[3] = "Heimer"
* name[1].period.end = "1960"
* name[2].use = #official
* name[2].family = "Smith"
* name[2].given[0] = "John"
* name[2].given[1] = "Jacob"
* name[2].given[2] = "Jingleheimer"
* name[2].period.start = "1960-01-01"
* gender = #other
* birthDate = "1923-07-25"
* address.state = "WI"
* address.country = "USA"