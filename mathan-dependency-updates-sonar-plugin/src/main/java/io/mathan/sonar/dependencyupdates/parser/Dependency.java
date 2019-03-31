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

package io.mathan.sonar.dependencyupdates.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class Dependency {


  private String groupId;
  private String artifactId;
  private String version;
  private String scope;
  private String classifier;
  private String type;
  private String next;
  private String last;
  private Availability availability;

  private List<String> incrementals = new ArrayList<>();
  private List<String> minors = new ArrayList<>();
  private List<String> majors = new ArrayList<>();


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Dependency that = (Dependency) o;
    return groupId.equals(that.groupId)
        && artifactId.equals(that.artifactId)
        && version.equals(that.version)
        && Objects.equals(scope, that.scope)
        && Objects.equals(classifier, that.classifier)
        && Objects.equals(type, that.type)
        && Objects.equals(next, that.next)
        && availability == that.availability
        && Objects.equals(incrementals, that.incrementals)
        && Objects.equals(minors, that.minors)
        && Objects.equals(majors, that.majors);
  }

  public Availability getAvailability() {
    return availability;
  }

  public List<String> getIncrementals() {
    return incrementals;
  }

  public List<String> getMajors() {
    return majors;
  }

  public List<String> getMinors() {
    return minors;
  }

  public int getUpdates() {
    return incrementals.size();
  }

  public int getUpgrades() {
    return minors.size() + majors.size();
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getClassifier() {
    return classifier;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getNext() {
    return next;
  }

  public String getLast() {
    return last;
  }

  public String getScope() {
    return scope;
  }

  public String getType() {
    return type;
  }

  public String getVersion() {
    return version;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public void setAvailability(Availability availability) {
    this.availability = availability;
  }

  public void setClassifier(@Nullable String classifier) {
    this.classifier = classifier;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public void setLast(String last) {
    this.last = last;
  }

  public void setScope(@Nullable String scope) {
    this.scope = scope;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public enum Availability {
    Incremental("incremental available"),
    Minor("minor available"),
    Major("major available"),
    None("no new available");
    private final String name;

    Availability(String name) {
      this.name = name;
    }

    /**
     * Creates the enum value for availability based on the status string for the dependency from the dependency-updates-report.
     */
    public static Availability fromString(String name) {
      Availability[] availabilities = Availability.values();
      for (Availability availability : availabilities) {
        if (name.equals(availability.name)) {
          return availability;
        }
      }
      return null;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, version, scope, classifier, type, next, availability, incrementals, minors, majors);
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s", groupId, artifactId, version);
  }
}
