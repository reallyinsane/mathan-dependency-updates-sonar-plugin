![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/io/mathan/sonar/mathan-dependency-updates-sonar-plugin/maven-metadata.xml.svg)
![example branch parameter](https://github.com/reallyinsane/mathan-dependency-updates-sonar-plugin/actions/workflows/maven.yml/badge.svg)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bcd46487fd2c4b79b930556275eec3d4)](https://www.codacy.com/app/reallyinsane/mathan-dependency-updates-sonar-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=reallyinsane/mathan-dependency-updates-sonar-plugin&amp;utm_campaign=Badge_Grade)
<a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/license-apache2-blue.svg"></a>

# Dependency-Updates-Report Plugin for SonarQube 7.9 to 8.4

Integrates [dependency updates report] from [versions-maven-plugin] into SonarQube v7.9. The plugin is compatible to SonarQube versions 7.9 to 8.4.

## About dependency updates report

The [versions-maven-plugin] has the goal *dependency-updates-report* which creates an overview about available updates for the dependencies of a Maven project. There can be incremental, minor or major version updates.


## Note

**This SonarQube plugin does not perform analysis**, rather, it reads existing dependency-updates-reports. Please refer to [versions-maven-plugin] for relevant documentation how to generate the reports.

## Metrics

The plugin keeps track of the following statistics:

Metric | Description 
-------|------------
Dependencies to patch | The number of dependencies with patches available (incremental updates). 
Dependencies to patch (Ratio) | The ratio of dependencies to patch. 
Dependencies to upgrade | The number of dependencies with upgrades available (minor and/or major updates).
Dependencies to upgrade (Ratio) | The ratio of dependencies to upgrade.
Dependencies Total | The total number of dependencies.
Patch maintenance | The rating of the patch maintenance (see below)
Patches missed | The total number of patches missed. 
Upgrade maintenance | The rating of the upgrade maintenance (see below)
Upgrades missed | The total number of upgrades missed. 

Please note that when computing measures on directory/module/project level measures for identical dependencies will be included only once. E.g. if a project contains two sub models having same
dependency, this is included in the measure for each sub module. For the project the measure will not include the dependency multiple times (for each sub module) but only once.

#### Maintenance rating

The maintenance rating is based on the ratio of dependencies with patches/upgrades and the total number of dependencies. The ratios of \<=5%, \<=10%, \<=20%, \<=50% and >50% are the guidelines to
define the rating. There are slightly adoptions for projects with less than 50 dependencies.


This metric is not final. For now the rating is calculated the following way. 

Ratings              | ![a](a.png) | ![b](b.png) | ![c](c.png) | ![d](d.png) | ![e](e.png)   
---------------------|-------------|-------------|-------------|-------------|------------
Ratio                | \<=~5%      |  \<=~10%    | \<=~20%     | \<=~50%     | \>50%
 0 - 10 dependencies | 0           |  1          |  2          |  3-5        | \>5
11 - 20 dependencies | 0-1         |  2-3        |  4-5        |  6-10       | \>10
21 - 50 dependencies | 0-2         |  3-5        |  6-10       |  11-25      | \>25
50 -    dependencies | 0-5         |  6-10       |  11-20      |  21-50      | \>50

## Installation

Copy the plugin (jar file) to $SONAR_INSTALL_DIR/extensions/plugins and restart SonarQube.

## Plugin Configuration

The [versions-maven-plugin] will output a file named 'dependency-updates-report.xml' when asked to output XML. The mathan-dependency-updates-sonar-plugin reads an existing dependency updates XML
report.

There is additional configuration available which enables to override the default mapping from available updates to SonarQube severity. It is also possible to include or exclude certain
dependencies for the check. Reducing or raising the severity for dependencies can be done too. 

### Artifact pattern syntax 
 
The filters defined are using a special artifact pattern syntax already known from Maven extended to allow a comma separated list of such patterns.
 
The pattern is defined like this: `[groupId]:[artifactId]:[type]:[version]:[scope]:[classifier]`. 

Each pattern segment is optional and supports full and partial * wildcards. An empty pattern segment is treated as an implicit wildcard. For example, `org.apache.*` would match all artifacts
whose group id started with `org.apache.`, and `:::*-SNAPSHOT` would match all snapshot artifacts.

### Configuration properties

This plugin offers various configuration options which are explained in the following categories. The settings can be found under Administration > Configuration > General Settings > Dependency-Updates.

#### Appearance

By default 9 metrics will be reported. With the following configuration metrics for ratio, rating and missed patches/upgrades can be hidden. Changes to the setting in this category need a restart of
 SonarQube to take effect.
 
Property | Default
---------|--------
Hide missed measures | false
Hide rating measures | false
Hide ratio mesasures | false 

#### Default Severity

For each kind of update for a dependency the default severity can be defined. This results in all issues for available updates of a kind to be created with this severity. All possible severities can
be used as value. (INFO, MINOR, MAJOR, CRITICAL, BLOCKER)

Property | Default
---------|--------
sonar.dependencyUpdates.updateIncremental | Severity.MINOR
sonar.dependencyUpdates.updateMinor | Severity.MAJOR
sonar.dependencyUpdates.updateMajor | Severity.CRITICAL

#### Inclusions/ Exclusions

By default updates for all dependencies are reported. A whitelist filter and/or a blacklist filter can be used to include/exclude certain dependencies. These filter use the artifact pattern syntax
described above. Some common use cases for the filter are

- exclude SNAPSHOT dependencies (`:::*-SNAPSHOT`)
- exclude dependencies with scope test (`::::test`)
- include dependencies of own company only (e.g `com.mycompany.*`)

Property | Default
---------|--------
sonar.dependencyUpdates.inclusions | `:::::` (include all)
sonar.dependencyUpdates.exclusions | (none)

#### Overrides

In addition to global inclusion/exclusion filter and the option to define the default severity for the kind of updates, overrides can be defined for all severities. Using such a whitelist filter
will report updates found for matching dependencies with the regarding severity. Some common use cases for the filter is

- increase severity for security related dependencies

Property | Default
---------|--------
sonar.dependencyUpdates.override.info | (none)
sonar.dependencyUpdates.override.minor | (none)
sonar.dependencyUpdates.override.major | (none)
sonar.dependencyUpdates.override.critical | (none)
sonar.dependencyUpdates.override.blocker | (none)

#### Versions

[versions-maven-plugin] by default reports all versions available in the configured repositories. Especially some libraries are releasing non-standard alpha, beta, release candidate or milestone 
versions. In general such libraries should not be reported by this plugin. Therefore the following configuration property is excluding these versions by default. It is also possible to configure this
for the [versions-maven-plugin] but then it has to be done for each project or global to maven.

Property | Default
---------|--------
sonar.dependencyUpdates.versionExclusionRegex | `.*\[-_\\.\]\(alpha\|Alpha\|ALPHA\|beta\|Beta\|BETA\|rc\|RC\|milestone\|M\|EA\)\[-_\\.\]?\[0-9\]*`

The second configuration in the Versions category is related to the sub versions reported for minor and major updated. [versions-maven-plugin] will report available patches for minor updates as
discrete versions as it will also report available minors for major updates. As if a minor or major update is done, usually the latest patch/minor update is taken respectively. So the following
configuration will exclude additional patches available for minor updates and additional minors available for major updates. It is enabled by default. 

Property | Default
---------|--------
sonar.dependencyUpdates.discreteMinorMajor | true

Sample for a dependency with version 1.1.0

Reported by [versions-maven-plugin] | Recognized (configuration is `false`) | Recognized (configuration is `true`)
------------------------------------|---------------------------------------|-------------------------------------
minor updates 1.2.0, 1.2.1, 1.2.2, 1.3.0 | 4 (1.2.0, 1.2.1, 1.2.2, 1.3.0) | 2 (1.2.2, 1.3.0)
major updates 2.0.0, 2.1.0, 2.2.0, 3.0.0, 4.0.0 | 5 (2.0.0, 2.1.0, 2.2.0, 3.0.0, 4.0.0) | 3 (2.2.0, 3.0.0, 4.0.0)

[dependency updates report]: https://www.mojohaus.org/versions-maven-plugin/dependency-updates-report-mojo.html
[versions-maven-plugin]: https://github.com/mojohaus/versions-maven-plugin
