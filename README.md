[![Build Status](https://travis-ci.org/reallyinsane/mathan-dependency-updates-sonar-plugin.svg?branch=master)](https://travis-ci.org/reallyinsane/mathan-dependency-updates-sonar-plugin)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bcd46487fd2c4b79b930556275eec3d4)](https://www.codacy.com/app/reallyinsane/mathan-dependency-updates-sonar-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=reallyinsane/mathan-dependency-updates-sonar-plugin&amp;utm_campaign=Badge_Grade)
<a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/license-apache2-blue.svg"></a>

Dependency-Updates-Report Plugin for SonarQube 7.x
==================================================

Integrates [dependency updates report] from [versions-maven-plugin] into SonarQube v7.3 or higher.

About dependency updates report
-------------------------------

Note
----
**This SonarQube plugin does not perform analysis**, rather, it reads existing dependency-updates-reports. Please refer to [versions-maven-plugin] for relevant documentation how to generate the reports.



[dependency updates report]: https://www.mojohaus.org/versions-maven-plugin/dependency-updates-report-mojo.html
[versions-maven-plugin]: https://github.com/mojohaus/versions-maven-plugin