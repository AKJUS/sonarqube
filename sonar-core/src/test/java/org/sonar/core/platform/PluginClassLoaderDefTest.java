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
package org.sonar.core.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginClassLoaderDefTest {

  @Test
  public void test_equals_and_hashCode() {
    PluginClassLoaderDef one = new PluginClassLoaderDef("one");
    PluginClassLoaderDef oneBis = new PluginClassLoaderDef("one");
    PluginClassLoaderDef two = new PluginClassLoaderDef("two");

    assertThat(one.equals(one)).isTrue();
    assertThat(one.equals(oneBis)).isTrue();
    assertThat(one)
      .hasSameHashCodeAs(one)
      .hasSameHashCodeAs(oneBis);

    assertThat(one.equals(two)).isFalse();
    assertThat(one.equals("one")).isFalse();
    assertThat(one.equals(null)).isFalse();
  }
}
