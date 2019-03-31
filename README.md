[![Build Status](https://travis-ci.org/reallyinsane/mathan-dependency-updates-sonar-plugin.svg?branch=master)](https://travis-ci.org/reallyinsane/mathan-dependency-updates-sonar-plugin)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bcd46487fd2c4b79b930556275eec3d4)](https://www.codacy.com/app/reallyinsane/mathan-dependency-updates-sonar-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=reallyinsane/mathan-dependency-updates-sonar-plugin&amp;utm_campaign=Badge_Grade)
<a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/license-apache2-blue.svg"></a>

# Dependency-Updates-Report Plugin for SonarQube 7.x

Integrates [dependency updates report] from [versions-maven-plugin] into SonarQube v7.3 or higher.

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
Patch maintenance | The rating of the patch maintenance (see below)
Patches missed | The total number of patches missed. 
Upgrade maintenance | The rating of the upgrade maintenance (see below)
Upgrades missed | The total number of upgrades missed. 

#### Maintenance rating

The number of dependencies of patches/upgrades cannot be used for rating as this should also depend on the ratio to the total number of dependencies and the total number of patches/upgrades per dependency.

This metric is not final. For now the rating is based on the regarding ratio. 

Ratio | Rating 
------|-----------
\<  5% |![a](a.png) 
\< 10% |![b](b.png)
\< 20% |![c](c.png)  
\< 50% |![d](d.png)
\>=50% |![e](e.png)

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

Property | Description | Default
---------|-------------|--------
sonar.dependencyUpdates.updateIncremental | Overrides the severity used for dependencies with incremental updates available. (INFO, MINOR, MAJOR, CRITICAL, BLOCKER) | Severity.MINOR
sonar.dependencyUpdates.updateMinor | Overrides the severity used for dependencies with minor updates available. (INFO, MINOR, MAJOR, CRITICAL, BLOCKER) | Severity.MAJOR
sonar.dependencyUpdates.updateMajor | Overrides the severity used for dependencies with major updates available. (INFO, MINOR, MAJOR, CRITICAL, BLOCKER) | Severity.CRITICAL
sonar.dependencyUpdates.inclusions | Filter (see Artifact pattern syntax) to include certain dependencies only. | `:::::` (include all)
sonar.dependencyUpdates.exclusions | Filter (see Artifact pattern syntax) to exclude certain dependencies. | (none)
sonar.dependencyUpdates.override.info | Filter (see Artifact pattern syntax) to override severtiy (if updates are available for dependencies matching) to INFO | (none)
sonar.dependencyUpdates.override.minor | Filter (see Artifact pattern syntax) to override severtiy (if updates are available for dependencies matching) to MINOR | (none)
sonar.dependencyUpdates.override.major | Filter (see Artifact pattern syntax) to override severtiy (if updates are available for dependencies matching) to MAJOR | (none)
sonar.dependencyUpdates.override.critical | Filter (see Artifact pattern syntax) to override severtiy (if updates are available for dependencies matching) to CRITICAL | (none)
sonar.dependencyUpdates.override.blocker | Filter (see Artifact pattern syntax) to override severtiy (if updates are available for dependencies matching) to BLOCKER | (none)

[dependency updates report]: https://www.mojohaus.org/versions-maven-plugin/dependency-updates-report-mojo.html
[versions-maven-plugin]: https://github.com/mojohaus/versions-maven-plugin