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
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public final class Metrics implements org.sonar.api.measures.Metrics {

  private static final Logger LOGGER = Loggers.get(Metrics.class);

  private static final String DOMAIN = "Dependency Updates";

  public static final String KEY_DEPENDENCIES = "metrics.dependencies";
  public static final String KEY_PATCHES = "metrics.patches";
  public static final String KEY_PATCHES_RATIO = "metrics.patches.ratio";
  public static final String KEY_PATCHES_MISSED = "metrics.patches.repeatedly";
  public static final String KEY_PATCHES_RATING = "metrios.patches.rating";
  public static final String KEY_UPGRADES = "metrics.upgrades";
  public static final String KEY_UPGRADES_RATIO = "metrics.upgrades.ratio";
  public static final String KEY_UPGRADES_MISSED = "metrics.upgrades.repeatedly";
  public static final String KEY_UPGRADES_RATING = "metrios.upgrades.rating";

  static final Metric<Integer> DEPENDENCIES = new Metric.Builder(Metrics.KEY_DEPENDENCIES, "Total dependencies", ValueType.INT)
      .setDescription("Total number of dependencies")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(Boolean.FALSE)
      .setDomain(Metrics.DOMAIN)
      .setHidden(true)
      .create();

  static final Metric<Integer> PATCHES = new Metric.Builder(Metrics.KEY_PATCHES, "Dependencies to patch", ValueType.INT)
      .setDescription("Dependencies with patches to apply")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Double> PATCHES_RATIO = new Metric.Builder(Metrics.KEY_PATCHES_RATIO, "Dependencies to patch (Ratio)", ValueType.PERCENT)
      .setDescription("Ratio of dependencies with patches")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Integer> PATCHES_MISSED = new Metric.Builder(Metrics.KEY_PATCHES_MISSED, "Patches missed", ValueType.INT)
      .setDescription("Total number of releases patches missed")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Integer> UPGRADES = new Metric.Builder(Metrics.KEY_UPGRADES, "Dependencies to upgrade", ValueType.INT)
      .setDescription("Dependencies with upgrades to apply")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Double> UPGRADES_RATIO = new Metric.Builder(Metrics.KEY_UPGRADES_RATIO, "Dependencies to upgrade (Ratio)", ValueType.PERCENT)
      .setDescription("Ratio of dependencies with upgrades")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Integer> UPGRADES_REPEATEDLY = new Metric.Builder(Metrics.KEY_UPGRADES_MISSED, "Upgrades missed", ValueType.INT)
      .setDescription("Total number of released upgrades missed")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Integer> PATCHES_RATING = new Metric.Builder(Metrics.KEY_PATCHES_RATING, "Patch Maintenance", ValueType.RATING)
      .setDescription("Rating of the maintenance of applying patches")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setWorstValue(5.0)
      .setBestValue(1.0)
      .create();

  static final Metric<Integer> UPGRADES_RATING = new Metric.Builder(Metrics.KEY_UPGRADES_RATING, "Upgrade Maintenance", ValueType.RATING)
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
  public static void calculateMetricsModule(SensorContext context, Analysis analysis) {
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
  }

  private static void calculateDependencies(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    context.<Integer>newMeasure().forMetric(Metrics.DEPENDENCIES).on(inputComponent).withValue(analysis.all().size()).save();
  }

  private static void calculatePatches(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int count = Math.toIntExact(analysis.all().stream().filter(dependency -> dependency.getIncrementals().size() > 0).count());
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES).on(inputComponent).withValue(count).save();
  }

  private static void calculatePatchesRatio(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    double ratio = 0;
    if (analysis.all().size() > 0) {
      ratio = 100.0 * analysis.all().stream().filter(dependency -> dependency.getIncrementals().size() > 0).count() / analysis.all().size();
    }
    context.<Double>newMeasure().forMetric(Metrics.PATCHES_RATIO).on(inputComponent).withValue(ratio).save();
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES_RATING).on(inputComponent).withValue(
        Math.toIntExact(calculateRatioRating(ratio))).save();
  }

  private static void calculatePatchesMissed(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int sum = Math.toIntExact(analysis.all().stream().collect(Collectors.summarizingInt(Dependency::getUpdates)).getSum());
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES_MISSED).on(inputComponent).withValue(sum).save();
  }

  private static void calculateUpgrades(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int count = Math.toIntExact(analysis.all().stream().filter(dependency -> dependency.getUpgrades() > 0).count());
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES).on(inputComponent).withValue(count).save();
  }

  private static void calculateUpgradesRatio(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    double ratio = 0;
    if (analysis.all().size() > 0) {
      ratio = 100.0 * analysis.all().stream().filter(dependency -> dependency.getUpgrades() > 0).count() / analysis.all().size();
    }
    context.<Double>newMeasure().forMetric(Metrics.UPGRADES_RATIO).on(inputComponent).withValue(ratio).save();
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES_RATING).on(inputComponent).withValue(
        Math.toIntExact(calculateRatioRating(ratio))).save();
  }

  private static void calculateUpgradesMissed(SensorContext context, InputComponent inputComponent, Analysis analysis) {
    int sum = Math.toIntExact(analysis.all().stream().collect(Collectors.summarizingInt(Dependency::getUpgrades)).getSum());
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES_REPEATEDLY).on(inputComponent).withValue(sum).save();
  }

  static int calculateRatioRating(double ratio) {
    if (ratio < 5) {
      return 1;
    } else if (ratio < 10) {
      return 2;
    } else if (ratio < 20) {
      return 3;
    } else if (ratio < 50) {
      return 4;
    } else {
      return 5;
    }
  }

  @Override
  public List<Metric> getMetrics() {
    return Arrays.asList(
        Metrics.DEPENDENCIES,
        Metrics.PATCHES,
        Metrics.PATCHES_RATIO,
        Metrics.PATCHES_MISSED,
        Metrics.PATCHES_RATING,
        Metrics.UPGRADES,
        Metrics.UPGRADES_RATIO,
        Metrics.UPGRADES_REPEATEDLY,
        Metrics.UPGRADES_RATING
    );
  }

}