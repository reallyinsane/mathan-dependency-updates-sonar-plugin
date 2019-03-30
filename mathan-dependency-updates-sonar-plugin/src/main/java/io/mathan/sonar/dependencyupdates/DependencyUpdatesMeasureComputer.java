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

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext
        .newDefinitionBuilder()
        .setOutputMetrics(
            Metrics.KEY_PATCHES,
            Metrics.KEY_PATCHES_MISSED,
            Metrics.KEY_UPGRADES,
            Metrics.KEY_UPGRADES_MISSED,
            Metrics.KEY_REFRESH_PERIOD)
        .build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    sumMeasure(context, Metrics.KEY_PATCHES);
    sumMeasure(context, Metrics.KEY_PATCHES_MISSED);
    sumMeasure(context, Metrics.KEY_UPGRADES);
    sumMeasure(context, Metrics.KEY_UPGRADES_MISSED);
    maxMeasure(context);
  }

  private void maxMeasure(MeasureComputerContext context) {
    if (context.getComponent().getType() != Type.FILE) {
      int max = 0;
      for (Measure m : context.getChildrenMeasures(Metrics.KEY_REFRESH_PERIOD)) {
        max = Math.max(max, m.getIntValue());
      }
      context.addMeasure(Metrics.KEY_REFRESH_PERIOD, max);
    }
  }

  private void sumMeasure(MeasureComputerContext context, String metricKey) {
    if (context.getComponent().getType() != Type.FILE) {
      int sum = 0;
      for (Measure m : context.getChildrenMeasures(metricKey)) {
        sum += m.getIntValue();
      }
      context.addMeasure(metricKey, sum);
    }
  }
}
