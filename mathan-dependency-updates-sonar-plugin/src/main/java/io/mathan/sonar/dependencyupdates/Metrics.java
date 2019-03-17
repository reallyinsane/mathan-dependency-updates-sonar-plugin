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
import java.util.List;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;

public final class Metrics implements org.sonar.api.measures.Metrics {

  private static final String DOMAIN = "Dependency Updates";

  private static final String KEY_INCREMENTAL_UPDATES = "incremental_updates";
  private static final String KEY_MINOR_UPDATES = "minor_updates";
  private static final String KEY_MAJOR_UPDATES = "major_updates";
  private static final String KEY_REPORT = "report_updates";

  static final Metric<Integer> INCREMENTAL_UPDATES = new Metric.Builder(Metrics.KEY_INCREMENTAL_UPDATES, "Incremental Updates", ValueType.INT)
      .setDescription("Dependencies with incremental updates")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Integer> MINOR_UPDATES = new Metric.Builder(Metrics.KEY_MINOR_UPDATES, "Minor Updates", ValueType.INT)
      .setDescription("Dependencies with minor updates")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  static final Metric<Integer> MAJOR_UPDATES = new Metric.Builder(Metrics.KEY_MAJOR_UPDATES, "Major Updates", ValueType.INT)
      .setDescription("Dependencies with major updates")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(Metrics.DOMAIN)
      .setBestValue(0.0)
      .create();

  public static final Metric<String> REPORT = new Metric.Builder(KEY_REPORT, "Dependency-Updates Report", Metric.ValueType.DATA)
      .setDescription("Report HTML")
      .setQualitative(Boolean.FALSE)
      .setDomain(Metrics.DOMAIN)
      .setHidden(false)
      .setDeleteHistoricalData(true)
      .create();

  @Override
  public List<Metric> getMetrics() {
    return Arrays.asList(
        Metrics.INCREMENTAL_UPDATES,
        Metrics.MINOR_UPDATES,
        Metrics.MAJOR_UPDATES,
        Metrics.REPORT
    );
  }
}