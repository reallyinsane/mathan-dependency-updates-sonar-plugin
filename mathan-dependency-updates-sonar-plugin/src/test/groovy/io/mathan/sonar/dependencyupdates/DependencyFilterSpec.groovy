package io.mathan.sonar.dependencyupdates

import io.mathan.sonar.dependencyupdates.parser.Dependency
import io.mathan.sonar.dependencyupdates.parser.Dependency.Availability
import org.sonar.api.batch.rule.Severity
import spock.lang.Specification

class DependencyFilterSpec extends Specification {

  /**
   * Tests the default behaviour of DependencyFilter. If the default configuration is used all values of Availability should
   * be mapped to the default severities.
   */
  def configuration(Availability availability, Severity expected) {
    expect:
      DependencyFilter filter = configuration()
      Dependency dependency = dependency(availability)
      filter.severity(dependency) == expected
    where:
      availability             | expected
      Availability.Incremental | Severity.MINOR
      Availability.Minor       | Severity.MAJOR
      Availability.Major       | Severity.CRITICAL
      Availability.None        | null
  }

  /**
   * Tests that if the default severity for {@link Availability#Incremental} is changed, this severity is used for
   * dependencies with {@link Availability#Incremental} and the default severities should be used for the remaining
   * availability values.
   */
  def "defaultSeverityIncremental"(Severity severityIncremental, Availability availability, Severity expected) {
    expect:
      DependencyFilter filter = configuration()
      filter.setDefaultSeverityIncremental(severityIncremental)
      Dependency dependency = dependency(availability)
      filter.severity(dependency) == expected
    where:
      severityIncremental | availability             | expected
      Severity.INFO       | Availability.None        | null
      Severity.INFO       | Availability.Incremental | Severity.INFO
      Severity.INFO       | Availability.Minor       | Severity.MAJOR
      Severity.INFO       | Availability.Major       | Severity.CRITICAL
      Severity.MAJOR      | Availability.Incremental | Severity.MAJOR
      Severity.CRITICAL   | Availability.Incremental | Severity.CRITICAL
      Severity.BLOCKER    | Availability.Incremental | Severity.BLOCKER
  }

  /**
   * Tests that if the default severity for {@link Availability#Minor} is changed, this severity is used for
   * dependencies with {@link Availability#Minor} and the default severities should be used for the remaining
   * availability values.
   */
  def "defaultSeverityMinor"(Severity severityMinor, Availability availability, Severity expected) {
    expect:
      DependencyFilter filter = configuration()
      filter.setDefaultSeverityMinor(severityMinor)
      Dependency dependency = dependency(availability)
      filter.severity(dependency) == expected
    where:
      severityMinor       | availability             | expected
      Severity.INFO       | Availability.None        | null
      Severity.INFO       | Availability.Incremental | Severity.MINOR
      Severity.INFO       | Availability.Minor       | Severity.INFO
      Severity.INFO       | Availability.Major       | Severity.CRITICAL
      Severity.MINOR      | Availability.Minor       | Severity.MINOR
      Severity.CRITICAL   | Availability.Minor       | Severity.CRITICAL
      Severity.BLOCKER    | Availability.Minor       | Severity.BLOCKER
  }

  /**
   * Tests that if the default severity for {@link Availability#Major} is changed, this severity is used for
   * dependencies with {@link Availability#Major} and the default severities should be used for the remaining
   * availability values.
   */
  def "defaultSeverityMajor"(Severity severityMajor, Availability availability, Severity expected) {
    expect:
      DependencyFilter filter = configuration()
      filter.setDefaultSeverityMajor(severityMajor)
      Dependency dependency = dependency(availability)
      filter.severity(dependency) == expected
    where:
      severityMajor       | availability             | expected
      Severity.INFO       | Availability.None        | null
      Severity.INFO       | Availability.Incremental | Severity.MINOR
      Severity.INFO       | Availability.Minor       | Severity.MAJOR
      Severity.INFO       | Availability.Major       | Severity.INFO
      Severity.MINOR      | Availability.Major       | Severity.MINOR
      Severity.BLOCKER    | Availability.Major       | Severity.BLOCKER
  }

  /**
   * Tests that if an inclusions filter is used, severity is determined only if inclusions filter matches the
   * dependency.
   */
  def "inclusion"(String inclusions, boolean included) {
    expect:
      DependencyFilter filter = configuration()
      filter.setInclusions(inclusions)
      Dependency dependency = dependency()
      Severity.MINOR.equals(filter.severity(dependency)) == included
    where:
      inclusions                                     | included
      ""                                             | false
      ":::"                                          | true
      "io.mathan.sonar.test:::"                      | true
      "io.mathan.sonar.test:test-artifact::"         | true
      "io.mathan.sonar.test:test-artifact::1.0.1"    | true
      "io.mathan.sonar.test:test-artifact:jar:1.0.1" | true
      "io.mathan.sonar.test:test-artifact:jar:1.0.2" | false
      "io.mathan.sonar.test:test-artifact:pom:1.0.1" | false
      "::pom:,io.mathan.sonar.test:::"               | true
  }

  /**
   * Tests that if an exclusions filter is used, severity is determined only if exclusions filter does not match the
   * dependency.
   */
  def "exclusions"(String exclusions, boolean included) {
    expect:
      DependencyFilter filter = configuration()
      filter.setExclusions(exclusions)
      Dependency dependency = dependency()
      Severity.MINOR.equals(filter.severity(dependency)) == included
    where:
      exclusions                                     | included
      ""                                             | true
      ":::"                                          | false
      "io.mathan.sonar.test:::"                      | false
      "io.mathan.sonar.test:test-artifact::"         | false
      "io.mathan.sonar.test:test-artifact::1.0.1"    | false
      "io.mathan.sonar.test:test-artifact:jar:1.0.1" | false
      "io.mathan.sonar.test:test-artifact:jar:1.0.2" | true
      "io.mathan.sonar.test:test-artifact:pom:1.0.1" | true
      "::pom:,io.mathan.sonar.test:::"               | false
  }

  def "overrides"(Availability availability, String info, String minor, String major, String critical, String blocker, Severity severity) {
    expect:
      DependencyFilter filter = configuration()
      filter.setOverrideInfo(info)
      filter.setOverrideMinor(minor)
      filter.setOverrideMajor(major)
      filter.setOverrideCritical(critical)
      filter.setOverrideBlocker(blocker)
      Dependency dependency = dependency(availability)
      filter.severity(dependency) == severity
    where:
      availability             | info  | minor | major | critical | blocker | severity
      Availability.None        | ":::" | ":::" | ":::" | ":::"    | ":::"   | null
      Availability.Incremental | ":::" | ""    | ""    | ""       | ""      | Severity.INFO
      Availability.Minor       | ":::" | ""    | ""    | ""       | ""      | Severity.INFO
      Availability.Major       | ":::" | ""    | ""    | ""       | ""      | Severity.INFO
      Availability.Incremental | ""    | ":::" | ""    | ""       | ""      | Severity.MINOR
      Availability.Minor       | ""    | ":::" | ""    | ""       | ""      | Severity.MINOR
      Availability.Major       | ""    | ":::" | ""    | ""       | ""      | Severity.MINOR
      Availability.Incremental | ""    | ""    | ":::" | ""       | ""      | Severity.MAJOR
      Availability.Minor       | ""    | ""    | ":::" | ""       | ""      | Severity.MAJOR
      Availability.Major       | ""    | ""    | ":::" | ""       | ""      | Severity.MAJOR
      Availability.Incremental | ""    | ""    | ""    | ":::"    | ""      | Severity.CRITICAL
      Availability.Minor       | ""    | ""    | ""    | ":::"    | ""      | Severity.CRITICAL
      Availability.Major       | ""    | ""    | ""    | ":::"    | ""      | Severity.CRITICAL
      Availability.Incremental | ""    | ""    | ""    | ""       | ":::"   | Severity.BLOCKER
      Availability.Minor       | ""    | ""    | ""    | ""       | ":::"   | Severity.BLOCKER
      Availability.Major       | ""    | ""    | ""    | ""       | ":::"   | Severity.BLOCKER
      Availability.Major       | ":::" | ":::" | ""    | ""       | ""      | Severity.MINOR
      Availability.Incremental | ":::" | ":::" | ":::" | ""       | ""      | Severity.MAJOR
      Availability.Incremental | ":::" | ":::" | ":::" | ":::"    | ""      | Severity.CRITICAL
      Availability.Incremental | ":::" | ":::" | ":::" | ":::"    | ":::"   | Severity.BLOCKER
  }

  DependencyFilter configuration() {
    DependencyFilter.create(Severity.MINOR, Severity.MAJOR, Severity.CRITICAL, ":::", "", "", "", "", "", "");
  }

  Dependency dependency(Availability availablility) {
    Dependency dependency = dependency()
    dependency.setAvailability(availablility)
    return dependency
  }

  Dependency dependency() {
    Dependency dependency = new Dependency();
    dependency.setGroupId("io.mathan.sonar.test")
    dependency.setArtifactId("test-artifact")
    dependency.setVersion("1.0.1")
    dependency.setType("jar")
    dependency.setAvailability(Availability.Incremental)
    return dependency
  }
}
