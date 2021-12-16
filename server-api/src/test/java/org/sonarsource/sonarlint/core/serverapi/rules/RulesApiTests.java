/*
 * SonarLint Server API
 * Copyright (C) 2016-2021 SonarSource SA
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
package org.sonarsource.sonarlint.core.serverapi.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Rules;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.serverapi.MockWebServerExtensionWithProtobuf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;

class RulesApiTests {

  @RegisterExtension
  static MockWebServerExtensionWithProtobuf mockServer = new MockWebServerExtensionWithProtobuf();

  private final ProgressMonitor progress = mock(ProgressMonitor.class);

  @Test
  void errorReadingRuleDescription() {
    mockServer.addStringResponse("/api/rules/show.protobuf?key=java:S1234", "trash");

    RulesApi rulesApi = new RulesApi(mockServer.serverApiHelper());

    var ruleDescription = rulesApi.getRuleDescription("java:S1234");
    assertThat(ruleDescription).isEmpty();
  }

  @Test
  void should_get_rule_description() {
    mockServer.addProtobufResponse("/api/rules/show.protobuf?key=java:S1234",
      Rules.ShowResponse.newBuilder().setRule(
        Rules.Rule.newBuilder()
          .setHtmlDesc("htmlDesc")
          .setHtmlNote("htmlNote")
          .build())
        .build());

    var rulesApi = new RulesApi(mockServer.serverApiHelper());

    var ruleDescription = rulesApi.getRuleDescription("java:S1234");

    assertThat(ruleDescription).contains("htmlDesc\nhtmlNote");
  }

  @Test
  void should_get_active_rules_of_a_given_quality_profile() {
    mockServer.addProtobufResponse(
      "/api/rules/search.protobuf?qprofile=QPKEY&organization=orgKey&activation=true&f=templateKey,actives&types=CODE_SMELL,BUG,VULNERABILITY&ps=500&p=1",
      Rules.SearchResponse.newBuilder()
        .setTotal(1)
        .setPs(1)
        .addRules(Rules.Rule.newBuilder().setKey("repo:key").setTemplateKey("template").build())
        .setActives(
          Rules.Actives.newBuilder()
            .putActives("repo:key", Rules.ActiveList.newBuilder().addActiveList(
              Rules.Active.newBuilder()
                .setSeverity("MAJOR")
                .addParams(Rules.Active.Param.newBuilder().setKey("paramKey").setValue("paramValue").build())
                .build())
              .build())
            .build())
        .build());

    var rulesApi = new RulesApi(mockServer.serverApiHelper("orgKey"));

    var activeRules = rulesApi.getAllActiveRules("QPKEY", progress);

    assertThat(activeRules)
      .extracting("ruleKey", "severity", "templateKey")
      .containsOnly(tuple("repo:key", "MAJOR", "template"));
    assertThat(activeRules)
      .flatExtracting("params")
      .extracting("key", "value")
      .containsOnly(tuple("paramKey", "paramValue"));
  }

}