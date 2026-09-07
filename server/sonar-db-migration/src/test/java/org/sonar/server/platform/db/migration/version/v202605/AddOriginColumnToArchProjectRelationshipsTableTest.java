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

import java.sql.SQLException;
import java.sql.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.db.MigrationDbTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.server.platform.db.migration.version.v202605.AddOriginColumnToArchProjectRelationshipsTable.COLUMN_ORIGIN;
import static org.sonar.server.platform.db.migration.version.v202605.AddOriginColumnToArchProjectRelationshipsTable.DEFAULT_ORIGIN;
import static org.sonar.server.platform.db.migration.version.v202605.AddOriginColumnToArchProjectRelationshipsTable.ORIGIN_SIZE;
import static org.sonar.server.platform.db.migration.version.v202605.AddOriginColumnToArchProjectRelationshipsTable.TABLE_NAME;

class AddOriginColumnToArchProjectRelationshipsTableTest {

  @RegisterExtension
  public final MigrationDbTester db = MigrationDbTester.createForMigrationStep(AddOriginColumnToArchProjectRelationshipsTable.class);

  private final AddOriginColumnToArchProjectRelationshipsTable underTest = new AddOriginColumnToArchProjectRelationshipsTable(db.database());

  @Test
  void execute_shouldAddNotNullableColumn() throws SQLException {
    db.assertColumnDoesNotExist(TABLE_NAME, COLUMN_ORIGIN);

    underTest.execute();

    db.assertColumnDefinition(TABLE_NAME, COLUMN_ORIGIN, Types.VARCHAR, ORIGIN_SIZE, false);
  }

  @Test
  void execute_shouldBeReentrant() throws SQLException {
    underTest.execute();
    underTest.execute();

    db.assertColumnDefinition(TABLE_NAME, COLUMN_ORIGIN, Types.VARCHAR, ORIGIN_SIZE, false);
  }

  @Test
  void execute_shouldDefaultOriginToManualOnInsert() throws SQLException {
    underTest.execute();

    insertRelationship("relation-uuid");

    assertThat(selectOrigin("relation-uuid")).isEqualTo(DEFAULT_ORIGIN);
  }

  private void insertRelationship(String uuid) {
    db.executeInsert(TABLE_NAME,
      "organization_id", "org-uuid",
      "uuid", uuid,
      "project_id", "project-uuid");
  }

  private String selectOrigin(String uuid) {
    var rows = db.select("SELECT origin FROM arch_proj_relations WHERE uuid = '" + uuid + "'");
    assertThat(rows).hasSize(1);
    return (String) rows.getFirst().get("ORIGIN");
  }
}
