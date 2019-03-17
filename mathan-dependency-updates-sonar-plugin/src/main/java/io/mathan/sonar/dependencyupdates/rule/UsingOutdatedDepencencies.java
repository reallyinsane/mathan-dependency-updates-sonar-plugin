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
package io.mathan.sonar.dependencyupdates.rule;

import io.mathan.sonar.dependencyupdates.Constants;
import javax.annotation.ParametersAreNonnullByDefault;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.server.rule.RulesDefinition;

public class UsingOutdatedDepencencies implements RulesDefinition {

  @Override
  @ParametersAreNonnullByDefault
  public void define(Context context) {
    NewRepository repo = context.createRepository(Constants.REPOSITORY_KEY, Constants.LANGUAGE_KEY);
    repo.setName("UsingOutdatedDependencies");
    NewRule rule = repo.createRule(Constants.RULE_KEY);
    rule.addTags("security", "vulnerability");
    rule.setName("Using outdated dependencies");
    rule.setSeverity(Severity.MAJOR);
    rule.setStatus(RuleStatus.READY);
    rule.addOwaspTop10(OwaspTop10.A9);

    String description = "<p>Dependencies, such as libraries, frameworks, and other software modules, will be almost outdated after a period of time. "+
        "In general dependencies should be updated on a regular basis. Applications using outdated dependencies will more likely be affected by vulnerabilities. "+
        "On the other hand applications updating their dependencies profit from vulnerabilities fixes faster.</p>";
    rule.setHtmlDescription(description);
    repo.done();
  }

}