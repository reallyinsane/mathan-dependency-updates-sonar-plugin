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
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public final class Metrics implements org.sonar.api.measures.Metrics {

  private static final Logger LOGGER = Loggers.get(Metrics.class);

  private static final String DOMAIN = "Dependency Updates";

  private static final String KEY_PATCHES = "metrics.patches";
  private static final String KEY_PATCHES_MISSED = "metrics.patches.repeatedly";
  private static final String KEY_UPGRADES = "metrics.upgrades";
  private static final String KEY_UPGRADES_MISSED = "metrics.upgrades.repeatedly";

  private static final String KEY_VERSION_DISTANCE = "metrics.version.distance";

  static final Metric<Integer> PATCHES = new Metric.Builder(Metrics.KEY_PATCHES, "Dependencies to patch", ValueType.INT)
      .setDescription("Dependencies with patches to apply")
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

  static final Metric<Integer> UPGRADES_REPEATEDLY = new Metric.Builder(Metrics.KEY_UPGRADES_MISSED, "Upgrades missed", ValueType.INT)
      .setDescription("Total number of released upgrades missed")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Integer> VERSION_DISTANCE = new Metric.Builder(Metrics.KEY_VERSION_DISTANCE, "Version Distance", ValueType.RATING)
      .setDescription("Version distance of all dependencies")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setWorstValue(5.0)
      .setBestValue(1.0)
      .create();

  /**
   * Calculates all metrics provided by this Sonar-Plugin based on the given Analysis.
   */
  public static void calculateMetrics(SensorContext context, Analysis analysis) {
    calculatePatches(context, analysis);
    calculatePatchesMissed(context, analysis);
    calculateUpgrades(context, analysis);
    calculateUpgradesMissed(context, analysis);
    calculateVersionDistance(context, analysis);
  }

  private static void calculateVersionDistance(SensorContext context, Analysis analysis) {
    int rating;
    int majors = Math.toIntExact(analysis.all().stream().filter(dependency -> dependency.getMajors().size() > 0).count());
    if (majors > 0) {
      rating = calculateVersionDistanceRatingMajor(majors);
    } else {
      int minors = Math.toIntExact(analysis.all().stream().filter(dependency -> dependency.getMinors().size() > 0).count());
      if (minors > 0) {
        rating = calculateVersionDistanceRatingMinor(minors);
      } else {
        int incrementals = Math.toIntExact(analysis.all().stream().filter(dependency -> dependency.getIncrementals().size() > 0).count());
        rating = calculateVersionDistanceRatingIncremental(incrementals);
      }
    }
    context.<Integer>newMeasure().forMetric(Metrics.VERSION_DISTANCE).on(context.module()).withValue(
        Math.toIntExact(rating)).save();
  }

  private static int calculateVersionDistanceRatingIncremental(int count) {
    switch (count) {
      case 0:
      case 1:
      case 2:
        return 1; // A
      case 3:
      case 4:
        return 2; // B
      case 5:
      case 6:
        return 3; // C
      case 7:
      case 8:
        return 4; // D
      default:
        return 5; // E
    }
  }

  private static int calculateVersionDistanceRatingMinor(int count) {
    switch (count) {
      case 0:
        return 1; // A
      case 1:
      case 2:
        return 2; // B
      case 3:
      case 4:
        return 3; // C
      case 5:
      case 6:
        return 4; // D
      default:
        return 5; // E
    }
  }

  private static int calculateVersionDistanceRatingMajor(int count) {
    switch (count) {
      case 0:
        return 1; // A
      case 1:
        return 2; // C
      case 2:
        return 3; // D
      default:
        return 5; // E
    }
  }

  @Override
  public List<Metric> getMetrics() {
    return Arrays.asList(
        Metrics.PATCHES,
        Metrics.PATCHES_MISSED,
        Metrics.UPGRADES,
        Metrics.UPGRADES_REPEATEDLY,
        Metrics.VERSION_DISTANCE
    );
  }

  private static void calculatePatches(SensorContext context, Analysis analysis) {
    long count = analysis.all().stream().filter(dependency -> dependency.getIncrementals().size() > 0).count();
    LOGGER.info("calculatePatches=" + count);
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES).on(context.module()).withValue(
        Math.toIntExact(count)).save();

  }

  private static void calculatePatchesMissed(SensorContext context, Analysis analysis) {
    long sum = analysis.all().stream().collect(Collectors.summarizingInt(Dependency::getUpdates)).getSum();
    LOGGER.info("calculatePatchesMissed=" + sum);
    context.<Integer>newMeasure().forMetric(Metrics.PATCHES_MISSED).on(context.module()).withValue(
        Math.toIntExact(sum)).save();
  }

  private static void calculateUpgrades(SensorContext context, Analysis analysis) {
    long count = analysis.all().stream().filter(dependency -> dependency.getUpgrades() > 0).count();
    LOGGER.info("calculateUpgrades=" + count);
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES).on(context.module()).withValue(Math.toIntExact(count)).save();
  }

  private static void calculateUpgradesMissed(SensorContext context, Analysis analysis) {
    long sum = analysis.all().stream().collect(Collectors.summarizingInt(Dependency::getUpgrades)).getSum();
    LOGGER.info("calculateUpgradesMissed=" + sum);
    context.<Integer>newMeasure().forMetric(Metrics.UPGRADES_REPEATEDLY).on(context.module()).withValue(
        Math.toIntExact(sum)).save();
  }

}