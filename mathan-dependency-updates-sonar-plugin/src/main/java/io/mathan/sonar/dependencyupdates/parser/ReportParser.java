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

import io.mathan.sonar.dependencyupdates.Utils;
import io.mathan.sonar.dependencyupdates.parser.Dependency.Availablility;
import java.io.InputStream;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ReportParser {

  private static final Logger LOGGER = Loggers.get(ReportParser.class);

  private ReportParser() {
  }

  public static Analysis parse(InputStream inputStream) throws XMLStreamException {

    SMInputFactory inputFactory = Utils.newStaxParser();
    SMHierarchicCursor rootC = inputFactory.rootElementCursor(inputStream);
    rootC.advance(); // <DependencyUpdatesReport>

    SMInputCursor childCursor = rootC.childCursor();

    Analysis analysis = new Analysis();

    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if ("summary".equals(nodeName)) {
        processSummary(analysis, childCursor);
      } else if ("dependencyManagements".equals(nodeName)) {
        processDependencies(analysis.getDependencyManagements(), childCursor, "dependencyManagement");
      } else if ("dependencies".equals(nodeName)) {
        processDependencies(analysis.getDependencies(), childCursor, "dependency");
      }
    }
    return analysis;
  }

  private static void processSummary(Analysis analysis, SMInputCursor summary) throws XMLStreamException {
    SMInputCursor childCursor = summary.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if ("usingLastVersion".equals(nodeName)) {
        analysis.setUsingLastVersion(Integer.valueOf(childCursor.collectDescendantText(true).trim()));
      } else if ("nextIncremetalAvailable".equals(nodeName)) {
        analysis.setNextIncrementalAvailable(Integer.valueOf(childCursor.collectDescendantText(true).trim()));
      } else if ("nextMinorAvailable".equals(nodeName)) {
        analysis.setNextMinorAvailable(Integer.valueOf(childCursor.collectDescendantText(true).trim()));
      } else if ("nextMajorAvailable".equals(nodeName)) {
        analysis.setNextMajorAvailable(Integer.valueOf(childCursor.collectDescendantText(true).trim()));
      }
    }
  }

  private static void processDependencies(List<Dependency> list, SMInputCursor parent, String childName) throws XMLStreamException {
    SMInputCursor childCursor = parent.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if (childName.equals(nodeName)) {
        list.add(processDependency(childCursor));
      }
    }
  }

  private static Dependency processDependency(SMInputCursor cursor) throws XMLStreamException {
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
        processVersions(dependency.getIncrementals(), childCursor, "incremental");
      } else if ("minors".equals(nodeName)) {
        processVersions(dependency.getMinors(), childCursor, "minor");
      } else if ("majors".equals(nodeName)) {
        processVersions(dependency.getMajors(), childCursor, "major");
      } else if ("status".equals(nodeName)) {
        dependency.setAvailablility(Availablility.fromString(StringUtils.trim(childCursor.collectDescendantText(true))));
      }

    }
    return dependency;
  }

  private static String nonNull(@Nullable String value) {
    if ("null".equals(value)) {
      return null;
    }
    return value;
  }

  private static void processVersions(List<String> versions, SMInputCursor cursor, String childName) throws XMLStreamException {
    SMInputCursor childCursor = cursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if (childName.equals(nodeName)) {
        versions.add(StringUtils.trim(childCursor.collectDescendantText(true)));
      }
    }
  }
}
