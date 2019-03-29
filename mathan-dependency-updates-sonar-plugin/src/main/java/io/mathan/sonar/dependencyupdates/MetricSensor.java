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
import io.mathan.sonar.dependencyupdates.parser.ReportParser;
import io.mathan.sonar.dependencyupdates.report.XmlReportFile;
import io.mathan.sonar.dependencyupdates.report.XmlReportFileImpl;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class MetricSensor implements org.sonar.api.batch.sensor.Sensor {
  private static final Logger LOGGER = Loggers.get(MetricSensor.class);
  private static final String SENSOR_NAME = "Dependency-Updates2";

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name(SENSOR_NAME).global();
  }

  @Override
  public void execute(SensorContext context) {
    String path = context.config().get(Constants.CONFIG_REPORT_PATH_PROPERTY).orElse(Constants.CONFIG_REPORT_PATH_DEFAULT);

    List<XmlReportFile> files = XmlReportFileImpl.getReports(context.fileSystem());
    try {
      Analysis analysis = ReportParser.parse(files);
      Metrics.calculateMetrics(context, analysis);
    } catch (IOException e) {
      LOGGER.warn("Analysis aborted due to: IO Errors", e);
    } catch (XMLStreamException e) {
      LOGGER.warn("Analysis aborted due to: XML is not valid", e);
    }
  }
}
