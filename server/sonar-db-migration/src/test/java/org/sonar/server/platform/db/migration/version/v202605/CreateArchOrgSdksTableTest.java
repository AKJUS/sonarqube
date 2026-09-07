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

import static org.sonar.server.platform.db.migration.def.VarcharColumnDef.UUID_SIZE;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.COLUMN_BOUNDARY_DESCRIPTORS;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.COLUMN_CREATED_AT;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.COLUMN_NAME;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.COLUMN_ORGANIZATION_ID;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.COLUMN_SDK_KEY;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.COLUMN_TARGET_COMPONENT_ID;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.COLUMN_UUID;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.INDEX_TARGET;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.INDEX_UUID;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.SHORT_TEXT_SIZE;
import static org.sonar.server.platform.db.migration.version.v202605.CreateArchOrgSdksTable.TABLE_NAME;

class CreateArchOrgSdksTableTest {

  @RegisterExtension
  public final MigrationDbTester db = MigrationDbTester.createForMigrationStep(CreateArchOrgSdksTable.class);

  private final CreateArchOrgSdksTable underTest = new CreateArchOrgSdksTable(db.database());

  @Test
  void migration_should_create_table() throws SQLException {
    db.assertTableDoesNotExist(TABLE_NAME);

    underTest.execute();

    db.assertTableExists(TABLE_NAME);
    db.assertPrimaryKey(TABLE_NAME, "pk_arch_org_sdks", COLUMN_ORGANIZATION_ID, COLUMN_UUID);
    db.assertColumnDefinition(TABLE_NAME, COLUMN_ORGANIZATION_ID, Types.VARCHAR, UUID_SIZE, false);
    db.assertColumnDefinition(TABLE_NAME, COLUMN_UUID, Types.VARCHAR, UUID_SIZE, false);
    db.assertColumnDefinition(TABLE_NAME, COLUMN_NAME, Types.VARCHAR, SHORT_TEXT_SIZE, false);
    db.assertColumnDefinition(TABLE_NAME, COLUMN_TARGET_COMPONENT_ID, Types.VARCHAR, UUID_SIZE, true);
    db.assertColumnDefinition(TABLE_NAME, COLUMN_SDK_KEY, Types.VARCHAR, SHORT_TEXT_SIZE, false);
    db.assertColumnDefinition(TABLE_NAME, COLUMN_BOUNDARY_DESCRIPTORS, Types.CLOB, null, false);
    db.assertColumnDefinition(TABLE_NAME, COLUMN_CREATED_AT, Types.BIGINT, null, false);
    db.assertIndex(TABLE_NAME, INDEX_UUID, COLUMN_UUID);
    db.assertIndex(TABLE_NAME, INDEX_TARGET, COLUMN_TARGET_COMPONENT_ID);
  }

  @Test
  void migration_should_be_reentrant() throws SQLException {
    db.assertTableDoesNotExist(TABLE_NAME);

    underTest.execute();
    underTest.execute();

    db.assertTableExists(TABLE_NAME);
  }
}
