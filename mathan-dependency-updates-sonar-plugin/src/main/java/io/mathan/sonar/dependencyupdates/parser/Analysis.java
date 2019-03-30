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

public class Analysis {

  private List<Dependency> dependencyManagements = new ArrayList<>();
  private List<Dependency> dependencies = new ArrayList<>();

  public List<Dependency> getDependencies() {
    return dependencies;
  }

  public List<Dependency> getDependencyManagements() {
    return dependencyManagements;
  }

  /**
   * Returns the complete list of all dependencies no matter if defined in dependency management or declared as depenendencies directly.
   */
  public List<Dependency> all() {
    List<Dependency> all = new ArrayList<>(dependencies);
    all.addAll(dependencyManagements);
    return all;
  }
}
