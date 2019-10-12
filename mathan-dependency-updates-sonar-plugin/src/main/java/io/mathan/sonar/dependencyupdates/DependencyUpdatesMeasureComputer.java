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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;

/**
 * As the metrics are reported for the pom.xml only, aggregation has to be made with this {@link MeasureComputer}.
 */
public class DependencyUpdatesMeasureComputer implements MeasureComputer {

  private static final Pattern PATTERN_PATCHES = Pattern.compile("[^:]*:[^:]*:[^:]*:([^:]*):[^:]*");
  private static final Pattern PATTERN_UPGRADES = Pattern.compile("[^:]*:[^:]*:[^:]*:[^:]*:([^:]*)");

  private static int extractMatches(Pattern pattern, String value) {
    Matcher matcher = pattern.matcher(value);
    if (matcher.matches()) {
      return Integer.parseInt(matcher.group(1));
    } else {
      return 0;
    }
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext
        .newDefinitionBuilder()
        .setOutputMetrics(
            Metrics.KEY_DEPENDENCIES,
            Metrics.KEY_DEPENDENCIES_DATA,
            Metrics.KEY_PATCHES,
            Metrics.KEY_PATCHES_DATA,
            Metrics.KEY_PATCHES_RATIO,
            Metrics.KEY_PATCHES_MISSED,
            Metrics.KEY_PATCHES_RATING,
            Metrics.KEY_UPGRADES,
            Metrics.KEY_UPGRADES_DATA,
            Metrics.KEY_UPGRADES_RATIO,
            Metrics.KEY_UPGRADES_MISSED,
            Metrics.KEY_UPGRADES_RATING)
        .build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    if (context.getComponent().getType() != Type.FILE) {
      int total = computeDependencies(context);
      computeDependencies(context, Metrics.KEY_PATCHES, Metrics.KEY_PATCHES_DATA, total, Metrics.KEY_PATCHES_MISSED, Metrics.KEY_PATCHES_RATIO, Metrics.KEY_PATCHES_RATING, PATTERN_PATCHES);
      computeDependencies(context, Metrics.KEY_UPGRADES, Metrics.KEY_UPGRADES_DATA, total, Metrics.KEY_UPGRADES_MISSED, Metrics.KEY_UPGRADES_RATIO, Metrics.KEY_UPGRADES_RATING, PATTERN_UPGRADES);
    }
  }

  private int computeDependencies(MeasureComputerContext context) {
    return uniqueDependencies(context, Metrics.KEY_DEPENDENCIES, Metrics.KEY_DEPENDENCIES_DATA).size();
  }

  private Set<String> uniqueDependencies(MeasureComputerContext context, String metric, String dataMetric) {
    Set<String> uniqueDependencies = new HashSet<>();
    for (Measure m : context.getChildrenMeasures(dataMetric)) {
      String dataMetricValue = m.getStringValue();
      if (!dataMetricValue.isEmpty()) {
        uniqueDependencies.addAll(Arrays.asList(m.getStringValue().split(",")));
      }
    }
    String measure = String.join(",", uniqueDependencies);
    context.addMeasure(dataMetric, measure);
    int total = uniqueDependencies.size();
    context.addMeasure(metric, total);
    return uniqueDependencies;
  }

  private void computeDependencies(MeasureComputerContext context, String metric, String dataMetric, int total, String missedMetric, String ratioMetric, String ratingMetric, Pattern pattern) {
    Set<String> uniqueDependencies = uniqueDependencies(context, metric, dataMetric);
    int missed = uniqueDependencies.stream().map(dataString -> extractMatches(pattern, dataString)).mapToInt(Integer::intValue).sum();
    context.addMeasure(missedMetric, missed);
    ratioRatingMeasure(context, ratioMetric, ratingMetric, uniqueDependencies.size(), total);
  }

  private void ratioRatingMeasure(MeasureComputerContext context, String ratioMetric, String ratingMetric, int count, int total) {
    double ratio = 0;
    if (total > 0) {
      ratio = 100 * count / total;
    }
    context.addMeasure(ratioMetric, ratio);
    context.addMeasure(ratingMetric, Metrics.calculateRating(count, total));
  }
}
