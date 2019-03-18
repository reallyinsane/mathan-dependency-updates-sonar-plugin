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

import io.mathan.sonar.dependencyupdates.parser.ReportParser;
import io.mathan.sonar.dependencyupdates.parser.Analysis;
import io.mathan.sonar.dependencyupdates.parser.Dependency;
import io.mathan.sonar.dependencyupdates.parser.Dependency.Availablility;
import io.mathan.sonar.dependencyupdates.report.XmlReportFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.log.Profiler;

public class Sensor implements org.sonar.api.batch.sensor.Sensor {

  private static final Logger LOGGER = Loggers.get(Sensor.class);
  private static final String SENSOR_NAME = "Dependency-Updates";

  private final FileSystem fileSystem;
  private final PathResolver pathResolver;

  public Sensor(FileSystem fileSystem, PathResolver pathResolver) {
    this.fileSystem = fileSystem;
    this.pathResolver = pathResolver;
  }

  private String formatDescription(Dependency dependency, boolean dependencyManagement) {
    StringBuilder sb = new StringBuilder();
    switch (dependency.getAvailablility()) {
      case Incremental:
        sb.append("Incremental ");
        break;
      case Minor:
        sb.append("Minor ");
        break;
      case Major:
        sb.append("Major ");
        break;
    }
    sb.append(String.format("update for dependency %s:%s:%s%s available. Next version is %s.", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(),
        dependencyManagement ? " (see dependency management)" : "", dependency.getNext()));
    return sb.toString().trim();
  }

  private void addIssues(SensorContext context, Map<Availablility, Severity> map, List<Dependency> dependencies, boolean dependencyManagement) {
    for (Dependency dependency : dependencies) {
      if (dependency.getAvailablility() != Availablility.None) {
        context.newIssue()
            .forRule(RuleKey.of(Constants.REPOSITORY_KEY, Constants.RULE_KEY))
            .at(new DefaultIssueLocation()
                .on(context.module())
                .message(formatDescription(dependency, dependencyManagement)))
            .overrideSeverity(map.get(dependency.getAvailablility()))
            .save();
      }
    }
  }

  private void addIssues(SensorContext context, Analysis analysis) {

    Severity severityIncremental = Severity.valueOf(context.config().get(Constants.CONFIG_UPDATE_INCREMENTAL).orElse(Constants.CONFIG_UPDATE_INCREMENTAL_DEFAULT));
    Severity severityMinor = Severity.valueOf(context.config().get(Constants.CONFIG_UPDATE_MINOR).orElse(Constants.CONFIG_UPDATE_MINOR_DEFAULT));
    Severity severityMajor = Severity.valueOf(context.config().get(Constants.CONFIG_UPDATE_MAJOR).orElse(Constants.CONFIG_UPDATE_MAJOR_DEFAULT));
    Map<Availablility, Severity> map = new HashMap<>();
    map.put(Availablility.Incremental, severityIncremental);
    map.put(Availablility.Minor, severityMinor);
    map.put(Availablility.Major, severityMajor);

    context.<Integer>newMeasure().forMetric(Metrics.INCREMENTAL_UPDATES).on(context.module()).withValue(analysis.getNextIncrementalAvailable()).save();
    context.<Integer>newMeasure().forMetric(Metrics.MINOR_UPDATES).on(context.module()).withValue(analysis.getNextMinorAvailable()).save();
    context.<Integer>newMeasure().forMetric(Metrics.MAJOR_UPDATES).on(context.module()).withValue(analysis.getNextMajorAvailable()).save();

    addIssues(context, map, analysis.getDependencyManagements(), true);
    addIssues(context, map, analysis.getDependencies(), false);


  }

  private Analysis parseAnalysis(SensorContext context) throws IOException, XMLStreamException {
    XmlReportFile report = XmlReportFile.getXmlReport(context.config(), fileSystem, this.pathResolver);
    return ReportParser.parse(report.getInputStream());
  }

  @Override
  public String toString() {
    return SENSOR_NAME;
  }

  @Override
  public void describe(SensorDescriptor sensorDescriptor) {
    sensorDescriptor.name(SENSOR_NAME);
  }

  @Override
  public void execute(SensorContext sensorContext) {
    Profiler profiler = Profiler.create(LOGGER);
    profiler.startInfo("Process Dependency-Updates report");
    try {
      Analysis analysis = parseAnalysis(sensorContext);
      addIssues(sensorContext, analysis);
    } catch (FileNotFoundException e) {
      LOGGER.info("Analysis skipped/aborted due to missing report file");
      LOGGER.debug(e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.warn("Analysis aborted due to: IO Errors", e);
    } catch (XMLStreamException e) {
      LOGGER.warn("Analysis aborted due to: XML is not valid", e);
    }
    profiler.stopInfo();
  }

}
