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
import org.sonar.server.platform.db.migration.sql.CreateTableBuilder;
import org.sonar.server.platform.db.migration.step.CreateTableChange;

import static org.sonar.server.platform.db.migration.def.BigIntegerColumnDef.newBigIntegerColumnDefBuilder;
import static org.sonar.server.platform.db.migration.def.VarcharColumnDef.UUID_SIZE;
import static org.sonar.server.platform.db.migration.def.VarcharColumnDef.newVarcharColumnDefBuilder;

public class CreateArchBoundariesTable extends CreateTableChange {

  static final String TABLE_NAME = "arch_boundaries";
  static final String COLUMN_ORGANIZATION_ID = "organization_id";
  static final String COLUMN_PROJECT_ID = "project_id";
  static final String COLUMN_BRANCH_ID = "branch_id";
  static final String COLUMN_BOUNDARY_KEY = "boundary_key";
  static final String COLUMN_DIRECTION = "direction";
  static final String COLUMN_CREATED_AT = "created_at";

  static final int SHORT_TEXT_SIZE = 255;
  static final int DIRECTION_SIZE = 20;

  protected CreateArchBoundariesTable(Database db) {
    super(db, TABLE_NAME);
  }

  @Override
  public void execute(Context context, String tableName) throws SQLException {
    context.execute(new CreateTableBuilder(getDialect(), tableName)
      .addPkColumn(newVarcharColumnDefBuilder().setColumnName(COLUMN_ORGANIZATION_ID).setLimit(UUID_SIZE).setIsNullable(false).build())
      .addPkColumn(newVarcharColumnDefBuilder().setColumnName(COLUMN_PROJECT_ID).setLimit(UUID_SIZE).setIsNullable(false).build())
      .addPkColumn(newVarcharColumnDefBuilder().setColumnName(COLUMN_BRANCH_ID).setLimit(UUID_SIZE).setIsNullable(false).build())
      .addPkColumn(newVarcharColumnDefBuilder().setColumnName(COLUMN_BOUNDARY_KEY).setLimit(SHORT_TEXT_SIZE).setIsNullable(false).build())
      .addPkColumn(newVarcharColumnDefBuilder().setColumnName(COLUMN_DIRECTION).setLimit(DIRECTION_SIZE).setIsNullable(false).build())
      .addColumn(newBigIntegerColumnDefBuilder().setColumnName(COLUMN_CREATED_AT).setIsNullable(false).build())
      .build());
  }
}
