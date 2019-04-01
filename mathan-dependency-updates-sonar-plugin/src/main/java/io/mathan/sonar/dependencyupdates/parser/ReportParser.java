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

import io.mathan.sonar.dependencyupdates.Constants;
import io.mathan.sonar.dependencyupdates.Utils;
import io.mathan.sonar.dependencyupdates.parser.Dependency.Availability;
import io.mathan.sonar.dependencyupdates.report.XmlReportFile;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ReportParser {

  private static final Logger LOGGER = Loggers.get(ReportParser.class);
  private final Configuration configuration;
  private final Pattern versionExclusionPattern;
  private final Pattern incrementalVersionsPattern = Pattern.compile("(.*)");
  private final Pattern minorVersionsPattern;
  private final Pattern majorVersionsPattern;

  public ReportParser(Configuration configuration) {
    this.configuration = configuration;
    this.versionExclusionPattern = Pattern.compile(configuration.get(Constants.CONFIG_VERSION_EXCLUSION_REGEX).orElse(Constants.CONFIG_VERSION_EXCLUSION_REGEX_DEFAULT));
    boolean discreteMinorMajor = configuration.getBoolean(Constants.CONFIG_DISCRETE_MINOR_MAJOR).orElse(Constants.CONFIG_DISCRETE_MINOR_MAJOR_DEFAULT);
    if (discreteMinorMajor) {
      minorVersionsPattern = Pattern.compile("^(\\d+\\.\\d+)");
      majorVersionsPattern = Pattern.compile("^(\\d+)");
    } else {
      minorVersionsPattern = Pattern.compile("(.*)");
      majorVersionsPattern = Pattern.compile("(.*)");
    }
  }

  /**
   * Creates an Analysis based on one or more dependency-update-reports.
   */
  public Analysis parse(List<XmlReportFile> files) throws IOException, XMLStreamException {
    Analysis analysis = new Analysis();
    for (XmlReportFile file : files) {
      parse(analysis, file);
    }
    return analysis;
  }

  private void parse(Analysis analysis, XmlReportFile file) throws IOException, XMLStreamException {
    SMInputFactory inputFactory = Utils.newStaxParser();
    SMHierarchicCursor rootC = inputFactory.rootElementCursor(file.getInputStream());
    rootC.advance(); // <DependencyUpdatesReport>

    SMInputCursor childCursor = rootC.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if ("dependencyManagements".equals(nodeName)) {
        processDependencies(analysis.getDependencyManagements(), childCursor, "dependencyManagement");
      } else if ("dependencies".equals(nodeName)) {
        processDependencies(analysis.getDependencies(), childCursor, "dependency");
      }
    }
  }


  private void processDependencies(List<Dependency> list, SMInputCursor parent, String childName) throws XMLStreamException {
    SMInputCursor childCursor = parent.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if (childName.equals(nodeName)) {
        list.add(processDependency(childCursor));
      }
    }
  }

  private Dependency processDependency(SMInputCursor cursor) throws XMLStreamException {
    Dependency dependency = new Dependency();
    SMInputCursor childCursor = cursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if ("groupId".equals(nodeName)) {
        dependency.setGroupId(StringUtils.trim(childCursor.collectDescendantText(true)));
      } else if ("artifactId".equals(nodeName)) {
        dependency.setArtifactId(StringUtils.trim(childCursor.collectDescendantText(true)));
      } else if ("scope".equals(nodeName)) {
        dependency.setScope(nonNull(StringUtils.trim(childCursor.collectDescendantText(true))));
      } else if ("classifier".equals(nodeName)) {
        dependency.setClassifier(nonNull(StringUtils.trim(childCursor.collectDescendantText(true))));
      } else if ("type".equals(nodeName)) {
        dependency.setType(StringUtils.trim(childCursor.collectDescendantText(true)));
      } else if ("currentVersion".equals(nodeName)) {
        dependency.setVersion(StringUtils.trim(childCursor.collectDescendantText(true)));
      } else if ("nextVersion".equals(nodeName)) {
        dependency.setNext(StringUtils.trim(childCursor.collectDescendantText(true)));
      } else if ("incrementals".equals(nodeName)) {
        dependency.getIncrementals().addAll(processVersions(incrementalVersionsPattern, childCursor, "incremental"));
      } else if ("minors".equals(nodeName)) {
        dependency.getMinors().addAll(processVersions(minorVersionsPattern, childCursor, "minor"));
      } else if ("majors".equals(nodeName)) {
        dependency.getMajors().addAll(processVersions(majorVersionsPattern, childCursor, "major"));
      } else if ("status".equals(nodeName)) {
        dependency.setAvailability(Availability.fromString(StringUtils.trim(childCursor.collectDescendantText(true))));
      }
    }
    if (dependency.getNext() != null && versionExclusionPattern.matcher(dependency.getNext()).matches()) {
      if (dependency.getIncrementals().size() > 0) {
        dependency.setNext(dependency.getIncrementals().get(0));
        dependency.setAvailability(Availability.Incremental);
      } else if (dependency.getMinors().size() > 0) {
        dependency.setNext(dependency.getMinors().get(0));
        dependency.setAvailability(Availability.Minor);
      } else if (dependency.getMajors().size() > 0) {
        dependency.setNext(dependency.getMajors().get(0));
        dependency.setAvailability(Availability.Major);
      } else {
        dependency.setNext(null);
        dependency.setAvailability(Availability.None);
      }
    }
    if (dependency.getMajors().size() > 0) {
      dependency.setLast(dependency.getMajors().get(dependency.getMajors().size() - 1));
    } else if (dependency.getMinors().size() > 0) {
      dependency.setLast(dependency.getMinors().get(dependency.getMinors().size() - 1));
    } else if (dependency.getIncrementals().size() > 0) {
      dependency.setLast(dependency.getIncrementals().get(dependency.getIncrementals().size() - 1));
    } else {
      dependency.setLast(dependency.getVersion());
    }

    return dependency;
  }

  private static String nonNull(@Nullable String value) {
    if ("null".equals(value)) {
      return null;
    }
    return value;
  }

  private Collection<String> processVersions(Pattern pattern, SMInputCursor cursor, String childName) throws XMLStreamException {
    Map<String, String> versions = new HashMap<>();
    SMInputCursor childCursor = cursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if (childName.equals(nodeName)) {
        String version = StringUtils.trim(childCursor.collectDescendantText(true));
        if (!versionExclusionPattern.matcher(version).matches()) {
          Matcher matcher = pattern.matcher(version);
          if (matcher.find()) {
            versions.put(matcher.group(1), version);
          }
        }
      }
    }
    return versions.values();
  }
}
