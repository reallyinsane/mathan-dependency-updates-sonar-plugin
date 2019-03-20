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

import io.mathan.sonar.dependencyupdates.parser.Dependency.Availability;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ReportParserTest {

  private static final String GROUP_ID = "io.mathan.test";

  @Test
  public void parseReport() throws Exception {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("report/dependency-updates-report.xml");
    Analysis analysis = ReportParser.parse(inputStream);
    Assert.assertEquals(1, analysis.getUsingLastVersion());
    Assert.assertEquals(2, analysis.getNextIncrementalAvailable());
    Assert.assertEquals(3, analysis.getNextMinorAvailable());
    Assert.assertEquals(4, analysis.getNextMajorAvailable());

    List<Dependency> dependencyManagements = analysis.getDependencyManagements();
    Assert.assertEquals(4, dependencyManagements.size());
    verifyPresent(
        dependencyManagements, "dependencymanagement-with-minor", null, null, "jar", "4.0.0", "4.1.0",
        Collections.emptyList(), Arrays.asList("4.1.0", "4.1.1", "4.1.2"), Collections.emptyList(), Availability.Minor);
    verifyPresent(
        dependencyManagements, "dependencymanagement-with-major", null, null, "pom", "1.4.0", "2.0.0",
        Collections.emptyList(), Collections.emptyList(), Arrays.asList("2.0.0", "2.1.0"), Availability.Major);
    verifyPresent(
        dependencyManagements, "dependencymanagement-with-incremental", null, null, "pom", "4.2.2", "4.2.3",
        Arrays.asList("4.2.3"), Collections.emptyList(), Collections.emptyList(), Availability.Incremental);
    verifyPresent(
        dependencyManagements, "dependencymanagement-with-none", null, null, "pom", "3.0.9", null,
        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Availability.None);

    List<Dependency> dependencies = analysis.getDependencies();
    Assert.assertEquals(4, dependencies.size());
    verifyPresent(
        dependencies, "dependency-with-minor", null, null, "pom", "4.0.0", "4.1.0",
        Collections.emptyList(), Arrays.asList("4.1.0", "4.1.1", "4.1.2"), Collections.emptyList(), Availability.Minor);
    verifyPresent(
        dependencies, "dependency-with-major", null, null, "pom", "1.4.0", "2.0.0",
        Collections.emptyList(), Collections.emptyList(), Arrays.asList("2.0.0", "2.1.0"), Availability.Major);
    verifyPresent(
        dependencies, "dependency-with-incremental", "test", null, "pom", "4.2.2", "4.2.3",
        Arrays.asList("4.2.3"), Collections.emptyList(), Collections.emptyList(), Availability.Incremental);
    verifyPresent(
        dependencies, "dependency-with-none", null, "any", "pom", "3.0.9", null,
        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Availability.None);

  }


  private static void verifyPresent(
      List<Dependency> dependencies, String artifactId, String scope, String classifier, String type,
      String version, String next, List<String> incrementals, List<String> minors, List<String> majors, Availability availability) {
    Dependency check = new Dependency();
    check.setArtifactId(artifactId);
    check.setAvailability(availability);
    check.setClassifier(classifier);
    check.setGroupId(ReportParserTest.GROUP_ID);
    check.setNext(next);
    check.setScope(scope);
    check.setType(type);
    check.setVersion(version);
    check.getIncrementals().addAll(incrementals);
    check.getMinors().addAll(minors);
    check.getMajors().addAll(majors);
    Assert.assertTrue(String.format("%s:%s:%s expected but not found", ReportParserTest.GROUP_ID, artifactId, version), dependencies.contains(check));
  }

}
