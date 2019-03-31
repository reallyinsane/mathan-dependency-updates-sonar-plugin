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

import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;

/**
 * As the metrics are reported for the pom.xml only, aggregation has to be made with this {@link MeasureComputer}.
 */
public class DependencyUpdatesMeasureComputer implements MeasureComputer {

  private static int getSum(MeasureComputerContext context, String metricKey) {
    int sum = 0;
    for (Measure m : context.getChildrenMeasures(metricKey)) {
      sum += m.getIntValue();
    }
    return sum;
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext
        .newDefinitionBuilder()
        .setOutputMetrics(
            Metrics.KEY_DEPENDENCIES,
            Metrics.KEY_PATCHES,
            Metrics.KEY_PATCHES_RATIO,
            Metrics.KEY_PATCHES_MISSED,
            Metrics.KEY_PATCHES_RATING,
            Metrics.KEY_UPGRADES,
            Metrics.KEY_UPGRADES_RATIO,
            Metrics.KEY_UPGRADES_MISSED,
            Metrics.KEY_UPGRADES_RATING)
        .build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    if (context.getComponent().getType() != Type.FILE) {
      sumMeasure(context, Metrics.KEY_DEPENDENCIES);
      sumMeasure(context, Metrics.KEY_PATCHES);
      sumMeasure(context, Metrics.KEY_PATCHES_MISSED);
      sumMeasure(context, Metrics.KEY_UPGRADES);
      sumMeasure(context, Metrics.KEY_UPGRADES_MISSED);
      ratioRatingMeasure(context, Metrics.KEY_PATCHES_RATIO, Metrics.KEY_PATCHES_RATING, Metrics.KEY_PATCHES, Metrics.KEY_DEPENDENCIES);
      ratioRatingMeasure(context, Metrics.KEY_UPGRADES_RATIO, Metrics.KEY_UPGRADES_RATING, Metrics.KEY_UPGRADES, Metrics.KEY_DEPENDENCIES);
    }
  }

  private void ratioRatingMeasure(MeasureComputerContext context, String ratioMetric, String ratingMetric, String countMetric, String totalMetric) {

    double ratio = 0;
    int count = getSum(context, countMetric);
    int total = getSum(context, totalMetric);
    if (total > 0) {
      ratio = 100 * count / total;
    }
    context.addMeasure(ratioMetric, ratio);
    context.addMeasure(ratingMetric, Metrics.calculateRatioRating(ratio));
  }

  private void maxMeasure(MeasureComputerContext context, String metricKey) {
    int max = 0;
    for (Measure m : context.getChildrenMeasures(metricKey)) {
      max = Math.max(max, m.getIntValue());
    }
    context.addMeasure(metricKey, max);
  }

  private void sumMeasure(MeasureComputerContext context, String metricKey) {
    context.addMeasure(metricKey, getSum(context, metricKey));
  }
}
