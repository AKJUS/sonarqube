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
import org.sonar.db.dialect.MsSql;
import org.sonar.db.dialect.Oracle;
import org.sonar.server.platform.db.migration.sql.DropMsSQLDefaultConstraintsBuilder;
import org.sonar.server.platform.db.migration.step.DdlChange;

public class RemoveDevopsPermsMappingDevopsPlatformDefaultValue extends DdlChange {

  private static final String TABLE_NAME = "devops_perms_mapping";
  private static final String COLUMN_NAME = "devops_platform";

  public RemoveDevopsPermsMappingDevopsPlatformDefaultValue(Database db) {
    super(db);
  }

  @Override
  public void execute(Context context) throws SQLException {
    if (MsSql.ID.equals(getDialect().getId())) {
      context.execute(new DropMsSQLDefaultConstraintsBuilder(getDatabase())
        .setTable(TABLE_NAME)
        .setColumns(COLUMN_NAME)
        .build());
    } else if (Oracle.ID.equals(getDialect().getId())) {
      context.execute("ALTER TABLE " + TABLE_NAME + " MODIFY (" + COLUMN_NAME + " DEFAULT NULL)");
    } else {
      // PostgreSQL and H2
      context.execute("ALTER TABLE " + TABLE_NAME + " ALTER COLUMN " + COLUMN_NAME + " DROP DEFAULT");
    }
  }
}
