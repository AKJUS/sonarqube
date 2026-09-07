/*
 * SonarQube
 * Copyright (C) SonarSource Sàrl
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
package org.sonar.server.almsettings.ws;

import org.junit.Test;
import org.sonar.server.almsettings.MultipleAlmFeature;
import org.sonar.server.exceptions.BadRequestException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class AlmSettingsSupportTest {

  private final AlmSettingsSupport underTest = new AlmSettingsSupport(null, null, null, mock(MultipleAlmFeature.class));

  @Test
  public void validateUrl_accepts_https_url() {
    assertThatCode(() -> underTest.validateUrl("https://dev.azure.com/myorg")).doesNotThrowAnyException();
  }

  @Test
  public void validateUrl_accepts_http_url() {
    assertThatCode(() -> underTest.validateUrl("http://azure.example.com")).doesNotThrowAnyException();
  }

  @Test
  public void validateUrl_rejects_null() {
    assertThatThrownBy(() -> underTest.validateUrl(null))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("Invalid URL: 'null'.");
  }

  @Test
  public void validateUrl_rejects_blank_string() {
    assertThatThrownBy(() -> underTest.validateUrl(""))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("Invalid URL: ''.");
  }

  @Test
  public void validateUrl_rejects_string_without_scheme() {
    assertThatThrownBy(() -> underTest.validateUrl("not a url"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("Invalid URL: 'not a url'.");
  }

  @Test
  public void validateUrl_rejects_unsupported_scheme() {
    assertThatThrownBy(() -> underTest.validateUrl("ftp://azure.example.com"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("Invalid URL: 'ftp://azure.example.com'.");
  }

  @Test
  public void validateAzureName_accepts_regular_name() {
    assertThatCode(() -> underTest.validateAzureName("projectName", "MyProject")).doesNotThrowAnyException();
  }

  @Test
  public void validateAzureName_accepts_name_with_spaces_and_dashes() {
    assertThatCode(() -> underTest.validateAzureName("repositoryName", "my repo-name_v2")).doesNotThrowAnyException();
  }

  @Test
  public void validateAzureName_rejects_forward_slash() {
    assertThatThrownBy(() -> underTest.validateAzureName("projectName", "foo/bar"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("'projectName' contains the invalid character '/'. Azure DevOps names must not contain any of: \\ / : < > | ? *");
  }

  @Test
  public void validateAzureName_rejects_backslash() {
    assertThatThrownBy(() -> underTest.validateAzureName("repositoryName", "foo\\bar"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("'repositoryName' contains the invalid character '\\'. Azure DevOps names must not contain any of: \\ / : < > | ? *");
  }

  @Test
  public void validateAzureName_rejects_pipe() {
    assertThatThrownBy(() -> underTest.validateAzureName("repositoryName", "bad|repo"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("'repositoryName' contains the invalid character '|'. Azure DevOps names must not contain any of: \\ / : < > | ? *");
  }

  @Test
  public void validateAzureName_rejects_colon() {
    assertThatThrownBy(() -> underTest.validateAzureName("projectName", "team:project"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("'projectName' contains the invalid character ':'. Azure DevOps names must not contain any of: \\ / : < > | ? *");
  }

  @Test
  public void validateAzureName_rejects_angle_brackets() {
    assertThatThrownBy(() -> underTest.validateAzureName("projectName", "team<x>project"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("'projectName' contains the invalid character '<'. Azure DevOps names must not contain any of: \\ / : < > | ? *");
  }

  @Test
  public void validateAzureName_rejects_question_mark() {
    assertThatThrownBy(() -> underTest.validateAzureName("projectName", "team?project"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("'projectName' contains the invalid character '?'. Azure DevOps names must not contain any of: \\ / : < > | ? *");
  }

  @Test
  public void validateAzureName_rejects_asterisk() {
    assertThatThrownBy(() -> underTest.validateAzureName("repositoryName", "wildcard*repo"))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("'repositoryName' contains the invalid character '*'. Azure DevOps names must not contain any of: \\ / : < > | ? *");
  }

}
