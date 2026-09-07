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
package org.sonar.server.v2.api.user.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.sonar.server.v2.common.model.UpdateField;

public class UserUpdateRestRequest {

  private UpdateField<String> login = UpdateField.undefined();
  private UpdateField<String> name = UpdateField.undefined();
  private UpdateField<String> email = UpdateField.undefined();
  private UpdateField<List<String>> scmAccounts = UpdateField.undefined();
  private UpdateField<String> externalProvider = UpdateField.undefined();
  private UpdateField<String> externalLogin = UpdateField.undefined();
  private UpdateField<String> externalId = UpdateField.undefined();
  private UpdateField<Boolean> local = UpdateField.undefined();

  @Size(min = 2, max = 100)
  @Schema(description = "User login", implementation = String.class)
  public UpdateField<String> getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = UpdateField.withValue(login);
  }

  @Size(max = 200)
  @Schema(description = "User first name and last name", implementation = String.class)
  public UpdateField<String> getName() {
    return name;
  }

  public void setName(String name) {
    this.name = UpdateField.withValue(name);
  }

  @Email
  @Size(min = 1, max = 100)
  @Schema(implementation = String.class, description = "Email")
  public UpdateField<String> getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = UpdateField.withValue(email);
  }

  @ArraySchema(arraySchema = @Schema(description = "List of SCM accounts."), schema = @Schema(implementation = String.class))
  public UpdateField<List<String>> getScmAccounts() {
    return scmAccounts;
  }

  public void setScmAccounts(List<String> scmAccounts) {
    this.scmAccounts = UpdateField.withValue(scmAccounts);
  }

  @Schema(implementation = String.class, description = """
    New identity provider. Only providers configured in your platform are supported. This could be: github, gitlab, bitbucket, saml, LDAP, LDAP_{serverKey}
    (according to your server configuration file).
    Warning: when this is updated, the user will only be able to authenticate using the new identity provider. The account is turned back into a local one
    only when 'externalProvider', 'externalLogin' and 'externalId' are all null or absent from the request; the resulting local account has no password
    (it was cleared when the account was originally bound to an external identity) and an administrator must set one before it can authenticate locally
    again. Binding a local account to an external identity provider requires this field to be explicitly set together with 'externalLogin' and/or 'externalId'.
    """)
  public UpdateField<String> getExternalProvider() {
    return externalProvider;
  }

  public void setExternalProvider(@Nullable String externalProvider) {
    this.externalProvider = UpdateField.withValue(externalProvider);
  }

  @Size(min = 1, max = 255)
  @Schema(implementation = String.class, description = "New external login, usually the login used in the authentication system.")
  public UpdateField<String> getExternalLogin() {
    return externalLogin;
  }

  public void setExternalLogin(@Nullable String externalLogin) {
    this.externalLogin = UpdateField.withValue(externalLogin);
  }

  @Size(min = 1, max = 255)
  @Schema(implementation = String.class, description = "New external id in the authentication system.")
  public UpdateField<String> getExternalId() {
    return externalId;
  }

  public void setExternalId(@Nullable String externalId) {
    this.externalId = UpdateField.withValue(externalId);
  }

  @Schema(implementation = Boolean.class, description = """
    Expected local status of the user once this update is applied. Provided as a safety check: if the resulting local status of the
    user does not match this value, the request is rejected instead of applied.
    """)
  public UpdateField<Boolean> getLocal() {
    return local;
  }

  public void setLocal(@Nullable Boolean local) {
    this.local = UpdateField.withValue(local);
  }
}
