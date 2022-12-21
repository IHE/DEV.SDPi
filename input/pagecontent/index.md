
**This Implementation Guide template includes skeleton pages for each part of an IHE supplement. The instructions for how to fill out these sections can be found in the included [Supplement Template](https://github.com/IHE/supplement-template/blob/master/suppl_template.md). This supplement template should not be included in your github repo or publication. It is included here for easy reference.**

**TODO: Write your executive overview here. Usually a one paragraph explanation of what problem this implementation guide solves.**

<div markdown="1" class="stu-note">

| [Significant Changes, Open, and Closed Issues](issues.html) |
{: .grid}

</div>

### Organization of This Guide
This guide is organized into the following sections:

1. Volume 1:
   1. [Introduction](volume-1.html)
   1. [Actors and Transactions](volume-1.html#actors-and-transactions)
   1. [Actor Options](volume-1.html#actor-options)
   1. [Actor Required Groupings](volume-1.html#required-groupings)
   1. [Overview](volume-1.html#overview)
   1. [Security Considerations](volume-1.html#security-considerations)
   1. [Cross Profile Considerations](volume-1.html#other-grouping)

2. Volume 2: Transaction Detail
   1. [FooBar do \[domain-YY\]](domain-YY.html)

3. Volume 3: Metadata and Content
  - **TODO: point to your content profiles if you have them**

4. Volume 4: National Extensions
  - **TODO: point at your national extensions if you have them**

5. Test Plan
  - [Test Plan](testplan.html)
    - **TODO: point to your appendix if you have them**
6. Other
  - [Changes to Other Profiles](other.html)
    - **TODO: Actor and transaction definitions, new glossary terms.**
    - **TODO: Any changes needed to other specifications.**


See also the [Table of Contents](toc.html) and
the index of [Artifacts](artifacts.html) defined as part of this implementation guide.

### Conformance Expectations

IHE uses the normative words: Shall, Should, and May according to [standards conventions](https://profiles.ihe.net/GeneralIntro/ch-E.html).

#### Must Support

The use of ```mustSupport``` in StructureDefinition profiles equivalent to the IHE use of **R2** as defined in [Appendix Z](https://profiles.ihe.net/ITI/TF/Volume2/ch-Z.html#z.10-profiling-conventions-for-constraints-on-fhir).

mustSupport of true - only has a meaning on items that are minimal cardinality of zero (0), and applies only to the source actor populating the data. The source actor shall populate the elements marked with MustSupport, if the concept is supported by the actor, a value exists, and security and consent rules permit. 
The consuming actors should handle these elements being populated or being absent/empty. 
Note that sometimes mustSupport will appear on elements with a minimal cardinality greater than zero (0), this is due to inheritance from a less constrained profile.

### Download 

You can also download:

* [this entire guide](full-ig.zip),
* the definition resources in [json](definitions.json.zip), [xml](definitions.xml.zip), [ttl](definitions.ttl.zip), or [csv](csvs.zip) format, or
* the example resources in [json](examples.json.zip), [xml](examples.xml.zip) or [ttl](examples.ttl.zip) format.

The source code for this Implementation Guide can be found on **TODO: Put your github repo here**

#### Cross Version Analysis

{% include cross-version-analysis.xhtml %}

#### Dependency Table

{% include dependency-table.xhtml %}

#### Globals Table

{% include globals-table.xhtml %}

#### IP Statements

{% include ip-statements.xhtml %}