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

import org.sonar.api.rule.Severity;

public final class Constants {

  public static final String CONFIG_REPORT_PATH_PROPERTY = "sonar.dependencyUpdates.reportPath";
  public static final String CONFIG_REPORT_PATH_DEFAULT = "target/"
      + "";

  static final String CONFIG_UPDATE_INCREMENTAL = "sonar.dependencyUpdates.updateIncremental";
  static final String CONFIG_UPDATE_INCREMENTAL_DEFAULT = Severity.MINOR;
  static final String CONFIG_UPDATE_MINOR = "sonar.dependencyUpdates.updateMinor";
  static final String CONFIG_UPDATE_MINOR_DEFAULT = Severity.MAJOR;
  static final String CONFIG_UPDATE_MAJOR = "sonar.dependencyUpdates.updateMajor";
  static final String CONFIG_UPDATE_MAJOR_DEFAULT = Severity.CRITICAL;

  static final String CONFIG_INCLUSIONS = "sonar.dependencyUpdates.inclusions";
  static final String CONFIG_EXCLUSIONS = "sonar.dependencyUpdates.exclusions";

  static final String CONFIG_OVERRIDE_INFO = "sonar.dependencyUpdates.override.info";
  static final String CONFIG_OVERRIDE_MINOR = "sonar.dependencyUpdates.override.minor";
  static final String CONFIG_OVERRIDE_MAJOR = "sonar.dependencyUpdates.override.major";
  static final String CONFIG_OVERRIDE_CRITICAL = "sonar.dependencyUpdates.override.critical";
  static final String CONFIG_OVERRIDE_BLOCKER = "sonar.dependencyUpdates.override.blocker";

  public static final String REPOSITORY_KEY = "DependencyUpdates";
  public static final String LANGUAGE_KEY = "mathan";
  public static final String RULE_KEY = "UsingDependencyWithAvailableUpdates";
  static final String SUB_CATEGORY_UPDATES = "Updates";
  static final String SUB_CATEGORY_INCLUSIONS_EXCLUSIONS = "Inclusions/Exclusions";
  static final String SUB_CATEGORY_OVERRIDES = "Overrides";

  private Constants() {
  }

}
