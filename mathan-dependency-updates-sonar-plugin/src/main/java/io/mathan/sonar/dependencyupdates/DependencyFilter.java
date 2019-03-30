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

import io.mathan.sonar.dependencyupdates.filter.PatternArtifactFilter;
import io.mathan.sonar.dependencyupdates.parser.Dependency;
import io.mathan.sonar.dependencyupdates.parser.Dependency.Availability;
import java.util.Arrays;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;

public class DependencyFilter {

  private Severity defaultSeverityIncremental;
  private Severity defaultSeverityMinor;
  private Severity defaultSeverityMajor;

  private ArtifactFilter inclusions;
  private ArtifactFilter exclusions;
  private ArtifactFilter overrideInfo;
  private ArtifactFilter overrideMinor;
  private ArtifactFilter overrideMajor;
  private ArtifactFilter overrideCritical;
  private ArtifactFilter overrideBlocker;

  static DependencyFilter create(Severity defaultSeverityIncremental, Severity defaultSeverityMinor, Severity defaultSeverityMajor,
      String inclusions, String exclusions, String overrideInfo, String overrideMinor, String overrideMajor, String overrideCritical, String overrideBlocker) {
    DependencyFilter filter = new DependencyFilter();
    filter.defaultSeverityIncremental = defaultSeverityIncremental;
    filter.defaultSeverityMinor = defaultSeverityMinor;
    filter.defaultSeverityMajor = defaultSeverityMajor;
    filter.inclusions = getIncludeFilter(inclusions);
    filter.exclusions = getIncludeFilter(exclusions);
    filter.overrideInfo = getIncludeFilter(overrideInfo);
    filter.overrideMinor = getIncludeFilter(overrideMinor);
    filter.overrideMajor = getIncludeFilter(overrideMajor);
    filter.overrideCritical = getIncludeFilter(overrideCritical);
    filter.overrideBlocker = getIncludeFilter(overrideBlocker);
    return filter;
  }


  static DependencyFilter create(SensorContext context) {
    return create(
        Severity.valueOf(context.config().get(Constants.CONFIG_UPDATE_INCREMENTAL).orElse(Constants.CONFIG_UPDATE_INCREMENTAL_DEFAULT)),
        Severity.valueOf(context.config().get(Constants.CONFIG_UPDATE_MINOR).orElse(Constants.CONFIG_UPDATE_MINOR_DEFAULT)),
        Severity.valueOf(context.config().get(Constants.CONFIG_UPDATE_MAJOR).orElse(Constants.CONFIG_UPDATE_MAJOR_DEFAULT)),
        context.config().get(Constants.CONFIG_INCLUSIONS).orElse(":::::"),
        context.config().get(Constants.CONFIG_EXCLUSIONS).orElse(""),
        context.config().get(Constants.CONFIG_OVERRIDE_INFO).orElse(""),
        context.config().get(Constants.CONFIG_OVERRIDE_MINOR).orElse(""),
        context.config().get(Constants.CONFIG_OVERRIDE_MAJOR).orElse(""),
        context.config().get(Constants.CONFIG_OVERRIDE_CRITICAL).orElse(""),
        context.config().get(Constants.CONFIG_OVERRIDE_BLOCKER).orElse(""));
  }

  public void setDefaultSeverityIncremental(Severity defaultSeverityIncremental) {
    this.defaultSeverityIncremental = defaultSeverityIncremental;
  }

  public void setDefaultSeverityMinor(Severity defaultSeverityMinor) {
    this.defaultSeverityMinor = defaultSeverityMinor;
  }

  public void setDefaultSeverityMajor(Severity defaultSeverityMajor) {
    this.defaultSeverityMajor = defaultSeverityMajor;
  }

  public void setInclusions(String inclusions) {
    this.inclusions = getIncludeFilter(inclusions);
  }

  public void setExclusions(String exclusions) {
    this.exclusions = getIncludeFilter(exclusions);
  }

  public void setOverrideInfo(String overrideInfo) {
    this.overrideInfo = getIncludeFilter(overrideInfo);
  }

  public void setOverrideMinor(String overrideMinor) {
    this.overrideMinor = getIncludeFilter(overrideMinor);
  }

  public void setOverrideMajor(String overrideMajor) {
    this.overrideMajor = getIncludeFilter(overrideMajor);
  }

  public void setOverrideCritical(String overrideCritical) {
    this.overrideCritical = getIncludeFilter(overrideCritical);
  }

  public void setOverrideBlocker(String overrideBlocker) {
    this.overrideBlocker = getIncludeFilter(overrideBlocker);
  }

  private static ArtifactFilter getIncludeFilter(String pattern) {
    if (pattern.trim().isEmpty()) {
      return new ExcludeArtifactFilter();
    } else {
      return new PatternArtifactFilter(Arrays.asList(pattern.split(",")));
    }
  }

  /**
   * Determines the severity of an issue to create for a dependency with updates available. The severity depends on the type of update available (incremental, minor, major) and the configuration of
   * this Sonar-Plugin.
   *
   * @param dependency The dependency to determine the severity for.
   * @return The Severity if an issue should be reported for the dependency or <code>null</code> if no issue should be created.
   */
  public Severity severity(Dependency dependency) {
    if (dependency.getAvailability() == Availability.None) {
      return null;
    }
    Artifact artifact = asArtifact(dependency);
    if (inclusions.include(artifact) && !exclusions.include(artifact)) {
      if (overrideBlocker.include(artifact)) {
        return Severity.BLOCKER;
      } else if (overrideCritical.include(artifact)) {
        return Severity.CRITICAL;
      } else if (overrideMajor.include(artifact)) {
        return Severity.MAJOR;
      } else if (overrideMinor.include(artifact)) {
        return Severity.MINOR;
      } else if (overrideInfo.include(artifact)) {
        return Severity.INFO;
      } else {
        switch (dependency.getAvailability()) {
          case Incremental:
            return defaultSeverityIncremental;
          case Minor:
            return defaultSeverityMinor;
          case Major:
            return defaultSeverityMajor;
          default:
            return null;
        }
      }
    } else {
      return null;
    }
  }

  private static Artifact asArtifact(Dependency dependency) {
    return new DefaultArtifact(
        dependency.getGroupId(),
        dependency.getArtifactId(),
        dependency.getVersion(),
        dependency.getScope(),
        dependency.getType(),
        dependency.getClassifier(),
        new DefaultArtifactHandler());
  }

  static class ExcludeArtifactFilter implements ArtifactFilter {

    @Override
    public boolean include(Artifact artifact) {
      return false;
    }
  }
}
