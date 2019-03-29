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
package io.mathan.sonar.dependencyupdates.filter;

import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * Filter to include or exclude artifacts by a given pattern. The artifact pattern syntax is influenced by the artifact pattern used in Maven and was extended to the following format:
 *
 * <pre>[groupId]:[artifactId]:[type]:[version]:[scope]:[classifier]</pre>
 */
public class PatternArtifactFilter implements ArtifactFilter {

  private final List<String> patterns;

  public PatternArtifactFilter(List<String> patterns) {
    this.patterns = patterns;
  }

  @Override
  public boolean include(Artifact artifact) {
    for (String pattern : patterns) {
      if (include(artifact, pattern)) {
        return true;
      }
    }
    return false;
  }

  private boolean include(Artifact artifact, String pattern) {
    String[] tokens = tokens(artifact);
    String[] parts = pattern.split(":");
    for (int i = 0; i < parts.length; i++) {
      if (!match(tokens[i], parts[i])) {
        return false;
      }
    }
    return true;
  }

  private boolean match(String token, String pattern) {
    if ("*".equals(pattern) || pattern.isEmpty()) {
      return true;
    } else if (pattern.startsWith("*") && pattern.endsWith("*")) {
      String contains = pattern.substring(1, pattern.length() - 1);
      return token.contains(contains);
    } else if (pattern.startsWith("*")) {
      String suffix = pattern.substring(1);
      return token.endsWith(suffix);
    } else if (pattern.endsWith("*")) {
      String prefix = pattern.substring(0, pattern.length() - 1);
      return token.startsWith(prefix);
    } else if (pattern.startsWith("[") || pattern.startsWith("(")) {
      try {
        return VersionRange.createFromVersionSpec(pattern).containsVersion(new DefaultArtifactVersion(token));
      } catch (InvalidVersionSpecificationException e) {
        return false;
      }
    } else {
      return token.equals(pattern);
    }
  }

  private String[] tokens(Artifact artifact) {
    return new String[]{
        artifact.getGroupId(),
        artifact.getArtifactId(),
        artifact.getType(),
        artifact.getBaseVersion(),
        artifact.getScope(),
        artifact.getClassifier()};
  }
}
