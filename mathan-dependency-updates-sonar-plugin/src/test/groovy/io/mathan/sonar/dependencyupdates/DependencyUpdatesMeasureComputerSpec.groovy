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
package io.mathan.sonar.dependencyupdates

import org.sonar.api.ce.measure.Component
import org.sonar.api.ce.measure.MeasureComputer
import org.sonar.api.ce.measure.test.TestComponent
import org.sonar.api.ce.measure.test.TestMeasureComputerContext
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinition
import org.sonar.api.ce.measure.test.TestSettings
import spock.lang.Specification

class DependencyUpdatesMeasureComputerSpec extends Specification {

  /**
   * Tests if metrics {@link Metrics#KEY_DEPENDENCIES} and {@link Metrics#KEY_DEPENDENCIES_DATA} are comuted properly based on the measures of child elements.
   * - measures of all children have to be taken into account
   * - only unique dependencies have to be taken into account
   * @param dependenciesA The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child A.
   * @param dependenciesB The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child B.
   * @param dependencies The expected value for measure {@link Metrics#KEY_DEPENDENCIES}.
   * @param dependenciesData The expected value for measure {@link Metrics#KEY_DEPENDENCIES_DATA}.
   */
  def dependencies(String dependenciesA, String dependenciesB, int dependencies, String dependenciesData) {
    expect:
    DependencyUpdatesMeasureComputer computer = new DependencyUpdatesMeasureComputer()
    MeasureComputer.MeasureComputerContext context = context(dependenciesA, dependenciesB)
    computer.compute(context)
    assert context.getMeasure(Metrics.KEY_DEPENDENCIES).intValue == dependencies
    assert sorted(dependenciesData).equals(sorted(context.getMeasure(Metrics.KEY_DEPENDENCIES_DATA).stringValue))
    where:
    dependenciesA | dependenciesB | dependencies | dependenciesData
    ""            | ""            | 0            | ""
    "a:b:c:0:0"   | ""            | 1            | "a:b:c:0:0"
    ""            | "a:b:c:0:0"   | 1            | "a:b:c:0:0"
    "a:b:c:0:0"   | "a:b:c:0:0"   | 1            | "a:b:c:0:0"
    "a:b:c:0:0"   | "a:b:c:1:0"   | 2            | "a:b:c:0:0,a:b:c:1:0"
    "a:b:c:0:0"   | "d:e:f:0:0"   | 2            | "a:b:c:0:0,d:e:f:0:0"
  }

  /**
   * Tests if metrics {@link Metrics#KEY_PATCHES}, {@link Metrics#KEY_PATCHES_DATA}, {@link Metrics#KEY_PATCHES_RATIO}, {@link Metrics#KEY_PATCHES_RATING} and {@link Metrics#KEY_PATCHES_MISSED} are
   * computed properly based on the measures of child elements.
   * - measures of all children have to be taken into account
   * - only unique dependencies have to be taken into account
   * @param dependenciesA The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child A.
   * @param dependenciesB The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child B.
   * @param patchesA The measure {@link Metrics#PATCHES_DATA} for child A.
   * @param patchesB The measure {@link Metrics#PATCHES_DATA} for child B.
   * @param patches The expected value for measure {@link Metrics#KEY_PATCHES}.
   * @param patchesData The expected value for measure {@link Metrics#KEY_PATCHES_DATA}.
   * @param patchesRatio The expected value for measure {@link Metrics#KEY_PATCHES_RATIO}.
   * @param patchesRating The expected value for measure {@link Metrics#KEY_PATCHES_RATING}.
   * @param patchesMissed The expected value for measure {@link Metrics#KEY_PATCHES_MISSED}.
   */
  def patches(String dependenciesA, String dependenciesB, String patchesA, String patchesB, int patches, String patchesData, int patchesRatio, int patchesRating, int patchesMissed) {
    expect:
    DependencyUpdatesMeasureComputer computer = new DependencyUpdatesMeasureComputer()
    MeasureComputer.MeasureComputerContext context = context(dependenciesA, dependenciesB, patchesA, patchesB, null, null)
    computer.compute(context)
    assert context.getMeasure(Metrics.KEY_PATCHES).intValue == patches
    assert context.getMeasure(Metrics.KEY_PATCHES_RATIO).doubleValue == patchesRatio
    assert context.getMeasure(Metrics.KEY_PATCHES_RATING).intValue == patchesRating
    assert context.getMeasure(Metrics.KEY_PATCHES_MISSED).intValue == patchesMissed
    assert sorted(patchesData).equals(sorted(context.getMeasure(Metrics.KEY_PATCHES_DATA).stringValue))
    where:
    dependenciesA | dependenciesB | patchesA    | patchesB    | patches | patchesData           | patchesRatio | patchesRating | patchesMissed
    ""            | ""            | ""          | ""          | 0       | ""                    | 0.0          | 1             | 0
    "a:b:c:1:0"   | ""            | "a:b:c:1:0" | ""          | 1       | "a:b:c:1:0"           | 100.0        | 2             | 1
    "a:b:c:1:0"   | "a:b:c:1:0"   | "a:b:c:1:0" | "a:b:c:1:0" | 1       | "a:b:c:1:0"           | 100.0        | 2             | 1
    "a:b:c:2:0"   | "a:b:c:2:0"   | "a:b:c:2:0" | "a:b:c:2:0" | 1       | "a:b:c:2:0"           | 100.0        | 2             | 2
    "a:b:c:1:0"   | "a:b:c:2:0"   | "a:b:c:1:0" | "a:b:c:2:0" | 2       | "a:b:c:1:0,a:b:c:2:0" | 100.0        | 3             | 3
    "a:b:c:4:0"   | "a:b:c:0:0"   | "a:b:c:4:0" | ""          | 1       | "a:b:c:4:0"           | 50.0         | 2             | 4
  }

  /**
   * Tests if metrics {@link Metrics#KEY_UPGRADES}, {@link Metrics#KEY_UPGRADES_DATA}, {@link Metrics#KEY_UPGRADES_RATIO}, {@link Metrics#KEY_UPGRADES_RATING} and {@link Metrics#KEY_UPGRADES_MISSED}
   * are computed properly based on the measures of child elements.
   * - measures of all children have to be taken into account
   * - only unique dependencies have to be taken into account
   * @param dependenciesA The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child A.
   * @param dependenciesB The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child B.
   * @param upgradesA The measure {@link Metrics#PATCHES_DATA} for child A.
   * @param upgradesB The measure {@link Metrics#PATCHES_DATA} for child B.
   * @param patches The expected value for measure {@link Metrics#KEY_UPGRADES}.
   * @param upgradesData The expected value for measure {@link Metrics#KEY_UPGRADES_DATA}.
   * @param upgradesRatio The expected value for measure {@link Metrics#KEY_UPGRADES_RATIO}.
   * @param upgradesRating The expected value for measure {@link Metrics#KEY_UPGRADES_RATING}.
   * @param upgradesMissed The expected value for measure {@link Metrics#KEY_UPGRADES_MISSED}.
   */
  def upgrades(String dependenciesA, String dependenciesB, String upgradesA, String upgradesB, int upgrades, String upgradesData, int upgradesRatio, int upgradesRating, int upgradesMissed) {
    expect:
    DependencyUpdatesMeasureComputer computer = new DependencyUpdatesMeasureComputer()
    MeasureComputer.MeasureComputerContext context = context(dependenciesA, dependenciesB, null, null, upgradesA, upgradesB)
    computer.compute(context)
    assert context.getMeasure(Metrics.KEY_UPGRADES).intValue == upgrades
    assert context.getMeasure(Metrics.KEY_UPGRADES_RATIO).doubleValue == upgradesRatio
    assert context.getMeasure(Metrics.KEY_UPGRADES_RATING).intValue == upgradesRating
    assert context.getMeasure(Metrics.KEY_UPGRADES_MISSED).intValue == upgradesMissed
    assert sorted(upgradesData).equals(sorted(context.getMeasure(Metrics.KEY_UPGRADES_DATA).stringValue))
    where:
    dependenciesA | dependenciesB | upgradesA   | upgradesB   | upgrades | upgradesData          | upgradesRatio | upgradesRating | upgradesMissed
    ""            | ""            | ""          | ""          | 0        | ""                    | 0.0           | 1              | 0
    "a:b:c:0:1"   | ""            | "a:b:c:0:1" | ""          | 1        | "a:b:c:0:1"           | 100.0         | 2              | 1
    "a:b:c:0:1"   | "a:b:c:0:1"   | "a:b:c:0:1" | "a:b:c:0:1" | 1        | "a:b:c:0:1"           | 100.0         | 2              | 1
    "a:b:c:0:2"   | "a:b:c:0:2"   | "a:b:c:0:2" | "a:b:c:0:2" | 1        | "a:b:c:0:2"           | 100.0         | 2              | 2
    "a:b:c:0:1"   | "a:b:c:0:2"   | "a:b:c:0:1" | "a:b:c:0:2" | 2        | "a:b:c:0:1,a:b:c:0:2" | 100.0         | 3              | 3
    "a:b:c:0:4"   | "a:b:c:0:0"   | "a:b:c:0:4" | ""          | 1        | "a:b:c:0:4"           | 50.0          | 2              | 4
  }

  /**
   * Creates a MeasureComputerContext with metric values for {@link Metrics#KEY_DEPENDENCIES_DATA}.
   * @param dependenciesA The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child A.
   * @param dependenciesB The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child B.
   */
  MeasureComputer.MeasureComputerContext context(String dependenciesA, String dependenciesB) {
    return context(dependenciesA, dependenciesB, null, null, null, null)
  }

  /**
   * Creates a MeasureComputerContext with metric values for {@link Metrics#KEY_DEPENDENCIES_DATA}, {@link Metrics#KEY_PATCHES_DATA} and {@link Metrics#KEY_UPGRADES_DATA}.
   * @param dependenciesA The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child A.
   * @param dependenciesB The measure {@link Metrics#KEY_DEPENDENCIES_DATA} for child B.
   * @param patchesA The measure {@link Metrics#PATCHES_DATA} for child A.
   * @param patchesB The measure {@link Metrics#PATCHES_DATA} for child B.
   * @param upgradesA The measure {@link Metrics#PATCHES_DATA} for child A.
   * @param upgradesB The measure {@link Metrics#PATCHES_DATA} for child B.
   * @return
   */
  MeasureComputer.MeasureComputerContext context(String dependenciesA, String dependenciesB, String patchesA, String patchesB, String upgradesA, String upgradesB) {
    TestComponent component = new TestComponent("key", Component.Type.DIRECTORY, null)
    TestSettings settings = new TestSettings()
    TestMeasureComputerDefinition.MeasureComputerDefinitionBuilderImpl builder = new TestMeasureComputerDefinition.MeasureComputerDefinitionBuilderImpl()
    builder.setInputMetrics(
        Metrics.KEY_DEPENDENCIES, Metrics.KEY_DEPENDENCIES_DATA, Metrics.KEY_PATCHES, Metrics.KEY_PATCHES_DATA, Metrics.KEY_PATCHES_MISSED, Metrics.KEY_PATCHES_RATING, Metrics.KEY_PATCHES_RATIO,
        Metrics.KEY_UPGRADES, Metrics.KEY_UPGRADES_DATA, Metrics.KEY_UPGRADES_MISSED, Metrics.KEY_UPGRADES_RATING, Metrics.KEY_UPGRADES_RATIO)
    builder.setOutputMetrics(
        Metrics.KEY_DEPENDENCIES, Metrics.KEY_DEPENDENCIES_DATA, Metrics.KEY_PATCHES, Metrics.KEY_PATCHES_DATA, Metrics.KEY_PATCHES_MISSED, Metrics.KEY_PATCHES_RATING, Metrics.KEY_PATCHES_RATIO,
        Metrics.KEY_UPGRADES, Metrics.KEY_UPGRADES_DATA, Metrics.KEY_UPGRADES_MISSED, Metrics.KEY_UPGRADES_RATING, Metrics.KEY_UPGRADES_RATIO)
    MeasureComputer.MeasureComputerDefinition definition = builder.build()

    TestMeasureComputerContext context = new TestMeasureComputerContext(component, settings, definition)
    context.addChildrenMeasures(Metrics.KEY_DEPENDENCIES_DATA, dependenciesA, dependenciesB)
    if (patchesA != null && patchesB != null) {
      context.addChildrenMeasures(Metrics.KEY_PATCHES_DATA, patchesA, patchesB)
    }
    if (upgradesA != null && upgradesB != null) {
      context.addChildrenMeasures(Metrics.KEY_UPGRADES_DATA, upgradesA, upgradesB)
    }
    return context
  }

  /**
   * Returns the list of dependencies sorted naturally.
   */
  String sorted(String dependencies) {
    Set<String> sorted = new TreeSet<>()
    Arrays.asList(dependencies.split(",")).forEach({ dependency -> sorted.add(dependency) })
    return String.join(",", sorted)
  }
}
