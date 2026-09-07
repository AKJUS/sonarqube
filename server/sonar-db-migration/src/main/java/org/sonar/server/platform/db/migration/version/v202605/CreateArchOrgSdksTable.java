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
import org.sonar.db.Database;
import org.sonar.server.platform.db.migration.sql.DynamoStyleTableBuilder;
import org.sonar.server.platform.db.migration.step.CreateTableChange;

import static org.sonar.server.platform.db.migration.def.BigIntegerColumnDef.newBigIntegerColumnDefBuilder;
import static org.sonar.server.platform.db.migration.def.ClobColumnDef.newClobColumnDefBuilder;
import static org.sonar.server.platform.db.migration.def.VarcharColumnDef.UUID_SIZE;
import static org.sonar.server.platform.db.migration.def.VarcharColumnDef.newVarcharColumnDefBuilder;

public class CreateArchOrgSdksTable extends CreateTableChange {

  static final String TABLE_NAME = "arch_org_sdks";
  static final String COLUMN_ORGANIZATION_ID = "organization_id";
  static final String COLUMN_UUID = "uuid";
  static final String COLUMN_NAME = "name";
  static final String COLUMN_TARGET_COMPONENT_ID = "target_component_id";
  static final String COLUMN_SDK_KEY = "sdk_key";
  static final String COLUMN_BOUNDARY_DESCRIPTORS = "boundary_descriptors";
  static final String COLUMN_CREATED_AT = "created_at";
  static final String INDEX_UUID = "arch_org_sdks_uuid";
  static final String INDEX_TARGET = "arch_org_sdks_target";

  static final int SHORT_TEXT_SIZE = 255;

  protected CreateArchOrgSdksTable(Database db) {
    super(db, TABLE_NAME);
  }

  @Override
  public void execute(Context context, String tableName) throws SQLException {
    context.execute(new DynamoStyleTableBuilder(getDialect(), tableName)
      .withPartitionKey(newVarcharColumnDefBuilder().setColumnName(COLUMN_ORGANIZATION_ID).setLimit(UUID_SIZE).setIsNullable(false).build())
      .withSortKey(newVarcharColumnDefBuilder().setColumnName(COLUMN_UUID).setLimit(UUID_SIZE).setIsNullable(false).build())
      .withAttribute(newVarcharColumnDefBuilder().setColumnName(COLUMN_NAME).setLimit(SHORT_TEXT_SIZE).setIsNullable(false).build())
      // Nullable: cleared (not deleted) when the project backing the target org component is deleted.
      .withAttribute(
        newVarcharColumnDefBuilder().setColumnName(COLUMN_TARGET_COMPONENT_ID).setLimit(UUID_SIZE).setIsNullable(true).build())
      .withAttribute(newVarcharColumnDefBuilder().setColumnName(COLUMN_SDK_KEY).setLimit(SHORT_TEXT_SIZE).setIsNullable(false).build())
      .withAttribute(newClobColumnDefBuilder().setColumnName(COLUMN_BOUNDARY_DESCRIPTORS).setIsNullable(false).build())
      .withAttribute(newBigIntegerColumnDefBuilder().setColumnName(COLUMN_CREATED_AT).setIsNullable(false).build())
      .withGlobalSecondaryIndex(INDEX_UUID, COLUMN_UUID)
      // Deleting an organization component has to find the SDKs pointing at it.
      .withGlobalSecondaryIndex(INDEX_TARGET, COLUMN_TARGET_COMPONENT_ID)
      .build());
  }
}
