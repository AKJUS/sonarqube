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
package org.sonar.server.platform.db.migration.version.v202605;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.db.Database;
import org.sonar.db.MigrationDbTester;
import org.sonar.db.dialect.Dialect;
import org.sonar.db.dialect.MsSql;
import org.sonar.db.dialect.Oracle;
import org.sonar.db.dialect.PostgreSql;
import org.sonar.server.platform.db.migration.step.DdlChange;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoveDevopsPermsMappingDevopsPlatformDefaultValueTest {

  private static final String TABLE_NAME = "devops_perms_mapping";
  private static final String COLUMN_NAME = "devops_platform";

  @RegisterExtension
  public final MigrationDbTester db = MigrationDbTester.createForMigrationStep(RemoveDevopsPermsMappingDevopsPlatformDefaultValue.class);

  private final RemoveDevopsPermsMappingDevopsPlatformDefaultValue underTest = new RemoveDevopsPermsMappingDevopsPlatformDefaultValue(db.database());

  @Test
  void execute_shouldKeepColumnNotNullable() throws SQLException {
    db.assertColumnDefinition(TABLE_NAME, COLUMN_NAME, Types.VARCHAR, 40, false);

    underTest.execute();

    db.assertColumnDefinition(TABLE_NAME, COLUMN_NAME, Types.VARCHAR, 40, false);
  }

  @Test
  void execute_shouldBeReentrant() throws SQLException {
    underTest.execute();
    underTest.execute();

    db.assertColumnDefinition(TABLE_NAME, COLUMN_NAME, Types.VARCHAR, 40, false);
  }

  @Test
  void execute_shouldDropDefaultSoOmittedInsertsFailInsteadOfSilentlyDefaultingToGithub() throws SQLException {
    underTest.execute();

    assertThatThrownBy(() -> db.executeInsert(TABLE_NAME,
      "uuid", "mapping-uuid",
      "devops_platform_role", "role-1",
      "sonarqube_permission", "user"))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void execute_whenOracle_shouldDropDefaultWithModify() throws SQLException {
    DdlChange.Context context = mock(DdlChange.Context.class);

    new RemoveDevopsPermsMappingDevopsPlatformDefaultValue(mockDatabase(Oracle.ID)).execute(context);

    verify(context).execute("ALTER TABLE " + TABLE_NAME + " MODIFY (" + COLUMN_NAME + " DEFAULT NULL)");
  }

  @Test
  void execute_whenPostgres_shouldDropDefaultWithAlterColumn() throws SQLException {
    DdlChange.Context context = mock(DdlChange.Context.class);

    new RemoveDevopsPermsMappingDevopsPlatformDefaultValue(mockDatabase(PostgreSql.ID)).execute(context);

    verify(context).execute("ALTER TABLE " + TABLE_NAME + " ALTER COLUMN " + COLUMN_NAME + " DROP DEFAULT");
  }

  @Test
  void execute_whenMsSql_shouldDropDefaultConstraint() throws SQLException {
    // On SQL Server the default is a named constraint that ALTER COLUMN leaves behind, so it must be looked up and
    // dropped by name.
    String constraintName = "DF_devops_perms_mapping_devops_platform";
    DdlChange.Context context = mock(DdlChange.Context.class);

    new RemoveDevopsPermsMappingDevopsPlatformDefaultValue(mockDatabase(MsSql.ID, connectionReturningDefaultConstraint(constraintName)))
      .execute(context);

    verify(context).execute(List.of("ALTER TABLE " + TABLE_NAME + " DROP CONSTRAINT " + constraintName));
  }

  private static Database mockDatabase(String dialectId) {
    Dialect dialect = mock(Dialect.class);
    when(dialect.getId()).thenReturn(dialectId);
    Database database = mock(Database.class);
    when(database.getDialect()).thenReturn(dialect);
    return database;
  }

  private static Database mockDatabase(String dialectId, Connection connection) throws SQLException {
    Database database = mockDatabase(dialectId);
    DataSource dataSource = mock(DataSource.class);
    when(dataSource.getConnection()).thenReturn(connection);
    when(database.getDataSource()).thenReturn(dataSource);
    return database;
  }

  /** A connection whose SQL Server default-constraint lookup returns a single constraint of the given name. */
  private static Connection connectionReturningDefaultConstraint(String constraintName) throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getString(1)).thenReturn(constraintName);
    PreparedStatement statement = mock(PreparedStatement.class);
    when(statement.executeQuery()).thenReturn(resultSet);
    Connection connection = mock(Connection.class);
    when(connection.getSchema()).thenReturn("dbo");
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    return connection;
  }
}
