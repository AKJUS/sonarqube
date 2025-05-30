/*
 * SonarQube
 * Copyright (C) 2009-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.scanner.externalissue.sarif;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.sarif.pojo.ReportingConfiguration;
import org.sonar.sarif.pojo.ReportingDescriptor;
import org.sonar.sarif.pojo.Result;
import org.sonar.sarif.pojo.Run;
import org.sonar.sarif.pojo.Tool;
import org.sonar.sarif.pojo.ToolComponent;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.sarif.pojo.Result.Level.WARNING;
import static org.sonar.scanner.externalissue.sarif.ResultMapper.DEFAULT_IMPACT_SEVERITY;
import static org.sonar.scanner.externalissue.sarif.ResultMapper.DEFAULT_SEVERITY;

class RulesSeverityDetectorTest {
  private static final String DRIVER_NAME = "Test";
  private static final String RULE_ID = "RULE_ID";

  @RegisterExtension
  private final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private final Run run = mock(Run.class);
  private final ReportingDescriptor rule = mock(ReportingDescriptor.class);
  private final Tool tool = mock(Tool.class);
  private final Result result = mock(Result.class);
  private final ToolComponent driver = mock(ToolComponent.class);
  private final ToolComponent extension = mock(ToolComponent.class);
  private final ReportingConfiguration defaultConfiguration = mock(ReportingConfiguration.class);

  @BeforeEach
  void setUp() {
    when(run.getResults()).thenReturn(List.of(result));
    when(run.getTool()).thenReturn(tool);
    when(tool.getDriver()).thenReturn(driver);
  }

  // We keep this test for backward compatibility until we remove the deprecated severity
  @Test
  void detectRulesSeverities_detectsCorrectlyResultDefinedRuleSeverities() {
    mockResultDefinedRuleSeverities();

    Map<String, Result.Level> rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeverities(run, DRIVER_NAME);

    assertNoLogs();
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId, tuple(RULE_ID, WARNING));
  }

  @Test
  void detectRulesSeveritiesForNewTaxonomy_shouldReturnsEmptyMapAndLogsWarning_whenOnlyResultDefinedRuleSeverities() {
    mockResultDefinedRuleSeverities();

    Map<String, Result.Level> rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeveritiesForNewTaxonomy(run, DRIVER_NAME);

    assertWarningLog(DEFAULT_IMPACT_SEVERITY.name());
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId);
  }

  @Test
  void detectRulesSeverities_detectsCorrectlyDriverDefinedRuleSeverities() {
    mockDriverDefinedRuleSeverities();

    Map<String, Result.Level> rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeveritiesForNewTaxonomy(run, DRIVER_NAME);

    assertNoLogs();
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId, tuple(RULE_ID, WARNING));

    // We keep this below for backward compatibility until we remove the deprecated severity
    rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeverities(run, DRIVER_NAME);

    assertNoLogs();
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId, tuple(RULE_ID, WARNING));
  }



  @ParameterizedTest
  @NullAndEmptySource
  void detectRulesSeverities_detectsCorrectlyExtensionsDefinedRuleSeverities(@Nullable Set<ReportingDescriptor> rules) {
    when(driver.getRules()).thenReturn(rules);
    mockExtensionsDefinedRuleSeverities();

    Map<String, Result.Level> rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeveritiesForNewTaxonomy(run, DRIVER_NAME);

    assertNoLogs();
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId, tuple(RULE_ID, WARNING));

    // We keep this below for backward compatibility until we remove the deprecated severity
    rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeverities(run, DRIVER_NAME);

    assertNoLogs();
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId, tuple(RULE_ID, WARNING));
  }

  @Test
  void detectRulesSeverities_returnsEmptyMapAndLogsWarning_whenUnableToDetectSeverities() {
    mockUnsupportedRuleSeveritiesDefinition();

    Map<String, Result.Level> rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeveritiesForNewTaxonomy(run, DRIVER_NAME);

    assertWarningLog(DEFAULT_IMPACT_SEVERITY.name());
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId);

    // We keep this below for backward compatibility until we remove the deprecated severity
    rulesSeveritiesByRuleId = RulesSeverityDetector.detectRulesSeverities(run, DRIVER_NAME);

    assertWarningLog(DEFAULT_SEVERITY.name());
    assertDetectedRuleSeverities(rulesSeveritiesByRuleId);
  }

  private void mockResultDefinedRuleSeverities() {
    when(run.getResults()).thenReturn(List.of(result));
    when(result.getLevel()).thenReturn(WARNING);
    when(result.getRuleId()).thenReturn(RULE_ID);
  }

  private void mockDriverDefinedRuleSeverities() {
    when(driver.getRules()).thenReturn(Set.of(rule));
    when(rule.getId()).thenReturn(RULE_ID);
    when(rule.getDefaultConfiguration()).thenReturn(defaultConfiguration);
    when(defaultConfiguration.getLevel()).thenReturn(ReportingConfiguration.Level.WARNING);
  }

  private void mockExtensionsDefinedRuleSeverities() {
    when(tool.getExtensions()).thenReturn(Set.of(extension));
    when(extension.getRules()).thenReturn(Set.of(rule));
    when(rule.getId()).thenReturn(RULE_ID);
    when(rule.getDefaultConfiguration()).thenReturn(defaultConfiguration);
    when(defaultConfiguration.getLevel()).thenReturn(ReportingConfiguration.Level.WARNING);
  }

  private void mockUnsupportedRuleSeveritiesDefinition() {
    when(run.getTool()).thenReturn(tool);
    when(tool.getDriver()).thenReturn(driver);
    when(driver.getRules()).thenReturn(Set.of());
    when(tool.getExtensions()).thenReturn(Set.of(extension));
    when(extension.getRules()).thenReturn(Set.of());
  }

  private void assertNoLogs() {
    assertThat(logTester.logs()).isEmpty();
  }

  private static void assertDetectedRuleSeverities(Map<String, Result.Level> severities, Tuple... expectedSeverities) {
    Assertions.assertThat(severities.entrySet())
      .extracting(Map.Entry::getKey, Map.Entry::getValue)
      .containsExactly(expectedSeverities);
  }

  private void assertWarningLog(String defaultSeverity) {
    assertThat(logTester.logs(Level.WARN))
      .contains(format("Unable to detect rules severity for issue detected by tool %s, falling back to default rule severity: %s", DRIVER_NAME, defaultSeverity));
  }

}
