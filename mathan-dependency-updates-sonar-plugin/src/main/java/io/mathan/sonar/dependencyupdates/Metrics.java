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

import io.mathan.sonar.dependencyupdates.parser.Analysis;
import io.mathan.sonar.dependencyupdates.parser.Dependency;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Range;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;

public final class Metrics implements org.sonar.api.measures.Metrics {

  private static final String DOMAIN = "Dependency Updates";

  static final String KEY_DEPENDENCIES = "metrics.dependencies";
  static final String KEY_DEPENDENCIES_DATA = "metrics.dependencies.data";
  static final String KEY_PATCHES = "metrics.patches";
  static final String KEY_PATCHES_DATA = "metrics.patches.data";
  static final String KEY_PATCHES_RATIO = "metrics.patches.ratio";
  static final String KEY_PATCHES_MISSED = "metrics.patches.repeatedly";
  static final String KEY_PATCHES_RATING = "metrios.patches.rating";
  static final String KEY_UPGRADES = "metrics.upgrades";
  static final String KEY_UPGRADES_DATA = "metrics.upgrades.data";
  static final String KEY_UPGRADES_RATIO = "metrics.upgrades.ratio";
  static final String KEY_UPGRADES_MISSED = "metrics.upgrades.repeatedly";
  static final String KEY_UPGRADES_RATING = "metrios.upgrades.rating";

  private static final int RATING_A = 1;
  private static final int RATING_B = 2;
  private static final int RATING_C = 3;
  private static final int RATING_D = 4;
  private static final int RATING_E = 5;

  private static Map<Range<Integer>, Map<Range<Integer>, Integer>> mapping = new HashMap<>();

  static {
    Range<Integer> range = Range.between(0, 10);
    Map<Range<Integer>, Integer> ratings = new HashMap<>();
    ratings.put(Range.between(0, 0), RATING_A);
    ratings.put(Range.between(1, 1), RATING_B);
    ratings.put(Range.between(2, 2), RATING_C);
    ratings.put(Range.between(3, 5), RATING_D);
    ratings.put(Range.between(6, 10), RATING_E);
    mapping.put(range, ratings);
    range = Range.between(11, 20);
    ratings = new HashMap<>();
    ratings.put(Range.between(0, 1), RATING_A);
    ratings.put(Range.between(2, 3), RATING_B);
    ratings.put(Range.between(4, 5), RATING_C);
    ratings.put(Range.between(6, 10), RATING_D);
    ratings.put(Range.between(11, 20), RATING_E);
    mapping.put(range, ratings);
    range = Range.between(21, 50);
    ratings = new HashMap<>();
    ratings.put(Range.between(0, 2), RATING_A);
    ratings.put(Range.between(3, 5), RATING_B);
    ratings.put(Range.between(6, 10), RATING_C);
    ratings.put(Range.between(11, 25), RATING_D);
    ratings.put(Range.between(26, 50), RATING_E);
    mapping.put(range, ratings);
    range = Range.between(51, Integer.MAX_VALUE);
    ratings = new HashMap<>();
    ratings.put(Range.between(0, 5), RATING_A);
    ratings.put(Range.between(6, 10), RATING_B);
    ratings.put(Range.between(11, 20), RATING_C);
    ratings.put(Range.between(21, 50), RATING_D);
    ratings.put(Range.between(51, Integer.MAX_VALUE), RATING_E);
    mapping.put(range, ratings);
  }


  private static final Metric<Integer> DEPENDENCIES = new Metric.Builder(Metrics.KEY_DEPENDENCIES, "Total dependencies", ValueType.INT)
      .setDescription("Total number of dependencies")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(Boolean.FALSE)
      .setDomain(Metrics.DOMAIN)
      .setHidden(false)
      .create();

  private static final Metric<String> DEPENDENCIES_DATA = new Metric.Builder(Metrics.KEY_DEPENDENCIES_DATA, "List of dependencies", ValueType.STRING)
      .setDescription("All dependencies concatenated in a list")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(false)
      .setDomain(Metrics.DOMAIN)
      .setHidden(true)
      .create();

  private static final Metric<Integer> PATCHES = new Metric.Builder(Metrics.KEY_PATCHES, "Dependencies to patch", ValueType.INT)
      .setDescription("Dependencies with patches to apply")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  private static final Metric<String> PATCHES_DATA = new Metric.Builder(Metrics.KEY_PATCHES_DATA, "List of patches", ValueType.STRING)
      .setDescription("All dependencies to patch concatenated in a list")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(false)
      .setDomain(Metrics.DOMAIN)
      .setHidden(true)
      .create();

  private static final Metric<Double> PATCHES_RATIO = new Metric.Builder(Metrics.KEY_PATCHES_RATIO, "Dependencies to patch (Ratio)", ValueType.PERCENT)
      .setDescription("Ratio of dependencies with patches")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  private static final Metric<Integer> PATCHES_MISSED = new Metric.Builder(Metrics.KEY_PATCHES_MISSED, "Patches missed", ValueType.INT)
      .setDescription("Total number of releases patches missed")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  private static final Metric<Integer> UPGRADES = new Metric.Builder(Metrics.KEY_UPGRADES, "Dependencies to upgrade", ValueType.INT)
      .setDescription("Dependencies with upgrades to apply")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  private static final Metric<String> UPGRADES_DATA = new Metric.Builder(Metrics.KEY_UPGRADES_DATA, "List of upgrades", ValueType.STRING)
      .setDescription("All dependencies to upgrade concatenated in a list")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(false)
      .setDomain(Metrics.DOMAIN)
      .setHidden(true)
      .create();

  private static final Metric<Double> UPGRADES_RATIO = new Metric.Builder(Metrics.KEY_UPGRADES_RATIO, "Dependencies to upgrade (Ratio)", ValueType.PERCENT)
      .setDescription("Ratio of dependencies with upgrades")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  private static final Metric<Integer> UPGRADES_REPEATEDLY = new Metric.Builder(Metrics.KEY_UPGRADES_MISSED, "Upgrades missed", ValueType.INT)
      .setDescription("Total number of released upgrades missed")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  private static final Metric<Integer> PATCHES_RATING = new Metric.Builder(Metrics.KEY_PATCHES_RATING, "Patch Maintenance", ValueType.RATING)
      .setDescription("Rating of the maintenance of applying patches")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setWorstValue(5.0)
      .setBestValue(1.0)
      .create();

  private static final Metric<Integer> UPGRADES_RATING = new Metric.Builder(Metrics.KEY_UPGRADES_RATING, "Upgrade Maintenance", ValueType.RATING)
      .setDescription("Rating of the maintenance of applying upgrades")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setWorstValue(5.0)
      .setBestValue(1.0)
      .create();

  /**
   * Calculates all metrics provided by this Sonar-Plugin based on the given Analysis.
   */
  static void calculateMetricsModule(SensorContext context, Analysis analysis) {
    calculateMetrics(context, context.fileSystem().inputFile(context.fileSystem().predicates().hasRelativePath("pom.xml")), analysis);
  }


  private static void calculateMetrics(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    calculateDependencies(context, inputComponent, analysis);
    calculatePatches(context, inputComponent, analysis);
    calculatePatchesRatio(context, inputComponent, analysis);
    calculatePatchesMissed(context, inputComponent, analysis);
    calculateUpgrades(context, inputComponent, analysis);
    calculateUpgradesRatio(context, inputComponent, analysis);
    calculateUpgradesMissed(context, inputComponent, analysis);
    calculateData(context, inputComponent, Metrics.DEPENDENCIES_DATA, analysis.all());
    calculateData(context, inputComponent, Metrics.PATCHES_DATA, analysis.all().stream().filter(dependency -> dependency.getUpdateCount() > 0).collect(Collectors.toList()));
    calculateData(context, inputComponent, Metrics.UPGRADES_DATA, analysis.all().stream().filter(dependency -> dependency.getUpgradeCount() > 0).collect(Collectors.toList()));
  }

  private static void calculateDependencies(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    context.<Integer>newMeasure().forMetric(Metrics.DEPENDENCIES).on(inputComponent).withValue(analysis.all().size()).save();
  }

  private static void calculateData(SensorContext context, InputComponent inputComponent, Metric<String> metric, List<Dependency> dependencies) {
    String dependenciesList = dependencies.stream().map(Dependency::toDataString).collect(Collectors.joining(","));
    context.<String>newMeasure().forMetric(metric).on(inputComponent).withValue(dependenciesList).save();
  }

  private static void calculatePatches(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int count = Math.toIntExact(analysis.all().stream().filter(dependency -> dependency.getIncrementals().size() > 0).count());
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES).on(inputComponent).withValue(count).save();
  }

  private static void calculatePatchesRatio(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    double ratio = 0;
    long totalDependenciesWithPatches = analysis.all().stream().filter(dependency -> dependency.getIncrementals().size() > 0).count();
    if (analysis.all().size() > 0) {
      ratio = 100.0 * totalDependenciesWithPatches / analysis.all().size();
    }
    context.<Double>newMeasure().forMetric(Metrics.PATCHES_RATIO).on(inputComponent).withValue(ratio).save();
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES_RATING).on(inputComponent).withValue(
        Math.toIntExact(calculateRating(Math.toIntExact(totalDependenciesWithPatches),analysis.all().size()))).save();
  }

  private static void calculatePatchesMissed(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int sum = Math.toIntExact(analysis.all().stream().collect(Collectors.summarizingInt(Dependency::getUpdateCount)).getSum());
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES_MISSED).on(inputComponent).withValue(sum).save();
  }

  private static void calculateUpgrades(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int count = Math.toIntExact(analysis.all().stream().filter(dependency -> dependency.getUpgradeCount() > 0).count());
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES).on(inputComponent).withValue(count).save();
  }

  private static void calculateUpgradesRatio(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    double ratio = 0;
    long totalDependenciesWithUpgrades = analysis.all().stream().filter(dependency -> dependency.getUpgradeCount() > 0).count();
    if (analysis.all().size() > 0) {
      ratio = 100.0 * totalDependenciesWithUpgrades / analysis.all().size();
    }
    context.<Double>newMeasure().forMetric(Metrics.UPGRADES_RATIO).on(inputComponent).withValue(ratio).save();
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES_RATING).on(inputComponent).withValue(
        Math.toIntExact(calculateRating(Math.toIntExact(totalDependenciesWithUpgrades),analysis.all().size()))).save();
  }

  private static void calculateUpgradesMissed(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int sum = Math.toIntExact(analysis.all().stream().collect(Collectors.summarizingInt(Dependency::getUpgradeCount)).getSum());
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES_REPEATEDLY).on(inputComponent).withValue(sum).save();
  }

  static int calculateRating(int withLater, int total) {
    Range range = mapping.keySet().stream().filter(r -> r.contains(total)).findFirst().get();
    Map<Range<Integer>, Integer> ratings = mapping.get(range);
    return ratings.get(ratings.keySet().stream().filter(r -> r.contains(withLater)).findFirst().get());
  }

  @Override
  public List<Metric> getMetrics() {
    return Arrays.asList(
        Metrics.DEPENDENCIES,
        Metrics.DEPENDENCIES_DATA,
        Metrics.PATCHES,
        Metrics.PATCHES_DATA,
        Metrics.PATCHES_RATIO,
        Metrics.PATCHES_MISSED,
        Metrics.PATCHES_RATING,
        Metrics.UPGRADES,
        Metrics.UPGRADES_DATA,
        Metrics.UPGRADES_RATIO,
        Metrics.UPGRADES_REPEATEDLY,
        Metrics.UPGRADES_RATING
    );
  }

}