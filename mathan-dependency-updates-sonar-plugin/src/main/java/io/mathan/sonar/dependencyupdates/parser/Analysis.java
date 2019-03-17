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

  private int usingLastVersion = 0;
  private int nextIncrementalAvailable = 0;
  private int nextMinorAvailable = 0;
  private int nextMajorAvailable = 0;

  private List<Dependency> dependencyManagements = new ArrayList<>();
  private List<Dependency> dependencies = new ArrayList<>();

  public List<Dependency> getDependencies() {
    return dependencies;
  }

  public List<Dependency> getDependencyManagements() {
    return dependencyManagements;
  }

  public int getNextIncrementalAvailable() {
    return nextIncrementalAvailable;
  }

  public int getNextMajorAvailable() {
    return nextMajorAvailable;
  }

  public int getNextMinorAvailable() {
    return nextMinorAvailable;
  }

  public int getUsingLastVersion() {
    return usingLastVersion;
  }

  public void setNextIncrementalAvailable(int nextIncrementalAvailable) {
    this.nextIncrementalAvailable = nextIncrementalAvailable;
  }

  public void setNextMajorAvailable(int nextMajorAvailable) {
    this.nextMajorAvailable = nextMajorAvailable;
  }

  public void setNextMinorAvailable(int nextMinorAvailable) {
    this.nextMinorAvailable = nextMinorAvailable;
  }

  public void setUsingLastVersion(int usingLastVersion) {
    this.usingLastVersion = usingLastVersion;
  }

}
