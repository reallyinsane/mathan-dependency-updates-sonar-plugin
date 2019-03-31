/*
 * mathan-dependency-updates-sonar-plugin
 * Copyright (c) 2019 Matthias Hanisch
 * matthias@mathan.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mathan.sonar.dependencyupdates;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.rule.Severity;

@ScannerSide
public class Configuration {

  private Configuration() {
    // do nothing
  }

  static List<PropertyDefinition> getPropertyDefinitions() {
    return Arrays.asList(
        PropertyDefinition.builder(Constants.CONFIG_REPORT_PATH_PROPERTY)
            .subCategory("Paths")
            .name("Dependency-Updates report path")
            .description("path to the 'dependency-updates-report.xml' file")
            .defaultValue(Constants.CONFIG_REPORT_PATH_DEFAULT)
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_UPDATE_INCREMENTAL)
            .subCategory(Constants.SUB_CATEGORY_UPDATES)
            .name("Incremental updates")
            .description("Severity used for available incremental updates")
            .options(Severity.ALL)
            .defaultValue(Constants.CONFIG_UPDATE_INCREMENTAL_DEFAULT)
            .type(PropertyType.SINGLE_SELECT_LIST)
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_UPDATE_MINOR)
            .subCategory(Constants.SUB_CATEGORY_UPDATES)
            .name("Minor updates")
            .description("Severity used for available minor updates")
            .options(Severity.ALL)
            .defaultValue(Constants.CONFIG_UPDATE_MINOR_DEFAULT)
            .type(PropertyType.SINGLE_SELECT_LIST)
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_UPDATE_MAJOR)
            .subCategory(Constants.SUB_CATEGORY_UPDATES)
            .name("Major updates")
            .description("Severity used for available major updates")
            .options(Severity.ALL)
            .defaultValue(Constants.CONFIG_UPDATE_MAJOR_DEFAULT)
            .type(PropertyType.SINGLE_SELECT_LIST)
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_INCLUSIONS)
            .subCategory(Constants.SUB_CATEGORY_INCLUSIONS_EXCLUSIONS)
            .name("Inclusions")
            .description("Whitelist of dependencies to include in the analysis separated by comma. The filter syntax is"
                + " [groupId]:[artifactId]:[type]:[version] where each pattern segment is optional and supports full"
                + " and partial * wildcards. Ab empty pattern segment is treated as an implicit wildcard *.")
            .defaultValue("")
            .type(PropertyType.STRING)
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_EXCLUSIONS)
            .subCategory(Constants.SUB_CATEGORY_INCLUSIONS_EXCLUSIONS)
            .name("Exclusions")
            .description("Blacklist of dependencies to exclude in the analysis separated by comma. The filter syntax is"
                + " [groupId]:[artifactId]:[type]:[version] where each pattern segment is optional and supports full"
                + " and partial * wildcards. Ab empty pattern segment is treated as an implicit wildcard *.")
            .defaultValue("")
            .type(PropertyType.STRING)
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_OVERRIDE_INFO)
            .subCategory(Constants.SUB_CATEGORY_OVERRIDES)
            .name("Override severity with INFO")
            .description("Whitelist of dependencies whose issue severity will be overridden with INFO. The filter syntax is"
                + " [groupId]:[artifactId]:[type]:[version] where each pattern segment is optional and supports full"
                + " and partial * wildcards. Ab empty pattern segment is treated as an implicit wildcard *.")
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_OVERRIDE_MINOR)
            .subCategory(Constants.SUB_CATEGORY_OVERRIDES)
            .name("Override severity with MINOR")
            .description("Whitelist of dependencies whose issue severity will be overridden with MINOR. The filter syntax is"
                + " [groupId]:[artifactId]:[type]:[version] where each pattern segment is optional and supports full"
                + " and partial * wildcards. Ab empty pattern segment is treated as an implicit wildcard *.")
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_OVERRIDE_MAJOR)
            .subCategory(Constants.SUB_CATEGORY_OVERRIDES)
            .name("Override severity with MAJOR")
            .description("Whitelist of dependencies whose issue severity will be overridden with MAJOR. The filter syntax is"
                + " [groupId]:[artifactId]:[type]:[version] where each pattern segment is optional and supports full"
                + " and partial * wildcards. Ab empty pattern segment is treated as an implicit wildcard *.")
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_OVERRIDE_CRITICAL)
            .subCategory(Constants.SUB_CATEGORY_OVERRIDES)
            .name("Override severity with CRITICAL")
            .description("Whitelist of dependencies whose issue severity will be overridden with CRITICAL. The filter syntax is"
                + " [groupId]:[artifactId]:[type]:[version] where each pattern segment is optional and supports full"
                + " and partial * wildcards. Ab empty pattern segment is treated as an implicit wildcard *.")
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_OVERRIDE_BLOCKER)
            .subCategory(Constants.SUB_CATEGORY_OVERRIDES)
            .name("Override severity with BLOCKER")
            .description("Whitelist of dependencies whose issue severity will be overridden with BLOCKER. The filter syntax is"
                + " [groupId]:[artifactId]:[type]:[version] where each pattern segment is optional and supports full"
                + " and partial * wildcards. Ab empty pattern segment is treated as an implicit wildcard *.")
            .build(),
        PropertyDefinition.builder(Constants.CONFIG_VERSION_EXCLUSION_REGEX)
            .subCategory(Constants.SUB_CATEGORY_GENERAL)
            .name("Version exlcude regex")
            .description("Regex to exclude version identifiers. This can be done in configuration of versions-maven-plugin or here."
                + " With the default filter version identifiers relating to alpha, beta, release candidate or milestone version"
                + " will be ignored.")
            .defaultValue(Constants.CONFIG_VERSION_EXCLUSION_REGEX_DEFAULT)
            .build()
    );
  }
}
