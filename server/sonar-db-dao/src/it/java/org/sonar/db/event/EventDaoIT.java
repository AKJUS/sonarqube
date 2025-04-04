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
package org.sonar.db.event;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.utils.System2;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.DbTester;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.component.ComponentTesting;
import org.sonar.db.component.SnapshotDto;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.db.component.SnapshotTesting.newAnalysis;
import static org.sonar.db.event.EventTesting.newEvent;

class EventDaoIT {

  @RegisterExtension
  private final DbTester dbTester = DbTester.create(System2.INSTANCE);

  private final DbClient dbClient = dbTester.getDbClient();
  private final DbSession dbSession = dbTester.getSession();
  private final EventDao underTest = dbTester.getDbClient().eventDao();

  @Test
  void select_by_uuid() {
    SnapshotDto analysis = dbTester.components().insertProjectAndSnapshot(ComponentTesting.newPrivateProjectDto());
    dbTester.events().insertEvent(newEvent(analysis).setUuid("A1"));
    dbTester.events().insertEvent(newEvent(analysis).setUuid("A2"));
    dbTester.events().insertEvent(newEvent(analysis).setUuid("A3"));

    Optional<EventDto> result = underTest.selectByUuid(dbSession, "A2");

    assertThat(result).isPresent();
    assertThat(result.get().getUuid()).isEqualTo("A2");
  }

  @Test
  void select_by_component_uuid() {
    ComponentDto project1 = ComponentTesting.newPrivateProjectDto();
    ComponentDto project2 = ComponentTesting.newPrivateProjectDto();
    SnapshotDto analysis1 = dbTester.components().insertProjectAndSnapshot(project1);
    SnapshotDto analysis2 = dbTester.components().insertProjectAndSnapshot(project2);
    String[] eventUuids1 = IntStream.range(0, 1 + new Random().nextInt(10))
      .mapToObj(i -> dbTester.events().insertEvent(newEvent(analysis1).setUuid("1_" + i)))
      .map(EventDto::getUuid)
      .toArray(String[]::new);
    EventDto event2 = dbTester.events().insertEvent(new EventDto()
      .setAnalysisUuid(analysis2.getUuid())
      .setComponentUuid(analysis2.getRootComponentUuid())
      .setName("name_2_")
      .setUuid("2_")
      .setCategory("cat_2_")
      .setDescription("desc_2_")
      .setData("dat_2_")
      .setDate(2_000L)
      .setCreatedAt(4_000L));

    assertThat(underTest.selectByComponentUuid(dbTester.getSession(), project1.uuid()))
      .extracting(EventDto::getUuid)
      .containsOnly(eventUuids1);
    List<EventDto> events2 = underTest.selectByComponentUuid(dbTester.getSession(), project2.uuid());
    assertThat(events2)
      .extracting(EventDto::getUuid)
      .containsOnly(event2.getUuid());
    assertThat(underTest.selectByComponentUuid(dbTester.getSession(), "does not exist"))
      .isEmpty();

    EventDto dto = events2.get(0);
    assertThat(dto.getUuid()).isEqualTo(event2.getUuid());
    assertThat(dto.getAnalysisUuid()).isEqualTo(event2.getAnalysisUuid());
    assertThat(dto.getComponentUuid()).isEqualTo(event2.getComponentUuid());
    assertThat(dto.getName()).isEqualTo(event2.getName());
    assertThat(dto.getCategory()).isEqualTo(event2.getCategory());
    assertThat(dto.getDescription()).isEqualTo(event2.getDescription());
    assertThat(dto.getData()).isEqualTo(event2.getData());
    assertThat(dto.getDate()).isEqualTo(event2.getDate());
    assertThat(dto.getCreatedAt()).isEqualTo(event2.getCreatedAt());
  }

  @Test
  void select_by_analysis_uuid() {
    ComponentDto project = ComponentTesting.newPrivateProjectDto();
    SnapshotDto analysis = dbTester.components().insertProjectAndSnapshot(project);
    SnapshotDto otherAnalysis = dbClient.snapshotDao().insert(dbSession, newAnalysis(project));
    dbTester.commit();
    dbTester.events().insertEvent(newEvent(analysis).setUuid("A1"));
    dbTester.events().insertEvent(newEvent(otherAnalysis).setUuid("O1"));
    dbTester.events().insertEvent(newEvent(analysis).setUuid("A2"));
    dbTester.events().insertEvent(newEvent(otherAnalysis).setUuid("O2"));
    dbTester.events().insertEvent(newEvent(analysis).setUuid("A3"));
    dbTester.events().insertEvent(newEvent(otherAnalysis).setUuid("O3"));

    List<EventDto> result = underTest.selectByAnalysisUuid(dbSession, analysis.getUuid());

    assertThat(result).hasSize(3);
    assertThat(result).extracting(EventDto::getUuid).containsOnly("A1", "A2", "A3");
  }

  @Test
  void select_by_analysis_uuids() {
    ComponentDto project = dbTester.components().insertPrivateProject().getMainBranchComponent();
    SnapshotDto a1 = dbTester.components().insertSnapshot(newAnalysis(project));
    SnapshotDto a2 = dbTester.components().insertSnapshot(newAnalysis(project));
    SnapshotDto a42 = dbTester.components().insertSnapshot(newAnalysis(project));
    dbTester.events().insertEvent(newEvent(newAnalysis(project)));
    dbTester.events().insertEvent(newEvent(a1).setUuid("A11"));
    dbTester.events().insertEvent(newEvent(a1).setUuid("A12"));
    dbTester.events().insertEvent(newEvent(a1).setUuid("A13"));
    dbTester.events().insertEvent(newEvent(a2).setUuid("A21"));
    dbTester.events().insertEvent(newEvent(a2).setUuid("A22"));
    dbTester.events().insertEvent(newEvent(a2).setUuid("A23"));
    dbTester.events().insertEvent(newEvent(a42).setUuid("AO1"));
    dbTester.events().insertEvent(newEvent(a42).setUuid("AO2"));
    dbTester.events().insertEvent(newEvent(a42).setUuid("AO3"));

    List<EventDto> result = underTest.selectByAnalysisUuids(dbSession, newArrayList(a1.getUuid(), a2.getUuid()));

    assertThat(result).hasSize(6);
    assertThat(result).extracting(EventDto::getUuid).containsOnly("A11", "A12", "A13", "A21", "A22", "A23");
  }

  @Test
  void return_different_categories() {
    ComponentDto project = ComponentTesting.newPrivateProjectDto();
    SnapshotDto analysis = dbTester.components().insertProjectAndSnapshot(project);
    List<EventDto> events = IntStream.range(0, 1 + new Random().nextInt(10))
      .mapToObj(i -> dbTester.events().insertEvent(newEvent(analysis).setCategory("cat_" + i)))
      .toList();

    List<EventDto> dtos = underTest.selectByComponentUuid(dbTester.getSession(), project.uuid());
    assertThat(dtos)
      .extracting(EventDto::getCategory)
      .containsOnly(events.stream().map(EventDto::getCategory).toArray(String[]::new));
  }

  @Test
  void insert() {
    EventDto expected = new EventDto()
      .setUuid("E1")
      .setAnalysisUuid("uuid_1")
      .setComponentUuid("ABCD")
      .setName("1.0")
      .setCategory(EventDto.CATEGORY_VERSION)
      .setDescription("Version 1.0")
      .setData("some data")
      .setDate(1413407091086L)
      .setCreatedAt(1225630680000L);
    underTest.insert(dbTester.getSession(), expected);
    dbTester.getSession().commit();

    EventDto dto = underTest.selectByUuid(dbSession, expected.getUuid()).get();

    assertThat(dto.getUuid()).isEqualTo(expected.getUuid());
    assertThat(dto.getAnalysisUuid()).isEqualTo(expected.getAnalysisUuid());
    assertThat(dto.getComponentUuid()).isEqualTo(expected.getComponentUuid());
    assertThat(dto.getName()).isEqualTo(expected.getName());
    assertThat(dto.getCategory()).isEqualTo(expected.getCategory());
    assertThat(dto.getDescription()).isEqualTo(expected.getDescription());
    assertThat(dto.getData()).isEqualTo(expected.getData());
    assertThat(dto.getDate()).isEqualTo(expected.getDate());
    assertThat(dto.getCreatedAt()).isEqualTo(expected.getCreatedAt());
  }

  @Test
  void update_name_and_description() {
    SnapshotDto analysis = dbTester.components().insertProjectAndSnapshot(ComponentTesting.newPrivateProjectDto());
    dbTester.events().insertEvent(newEvent(analysis).setUuid("E1"));

    underTest.update(dbSession, "E1", "New Name", "New Description");

    EventDto result = dbClient.eventDao().selectByUuid(dbSession, "E1").get();
    assertThat(result.getName()).isEqualTo("New Name");
    assertThat(result.getDescription()).isEqualTo("New Description");
  }

  @Test
  void givenSomeSqUpgradeEvents_whenRetrieved_shouldReturnCorrectlyOrderedByDateDescending() {
    long olderDate = 1L;
    long newerDate = 2L;

    ComponentDto componentDto = ComponentTesting.newPrivateProjectDto();
    SnapshotDto analysis = dbTester.components().insertProjectAndSnapshot(componentDto);
    dbTester.events().insertEvent(newEvent(analysis).setCategory(EventDto.CATEGORY_SQ_UPGRADE).setDate(olderDate).setUuid("E1"));
    dbTester.events().insertEvent(newEvent(analysis).setCategory(EventDto.CATEGORY_SQ_UPGRADE).setDate(newerDate).setUuid("E2"));

    List<EventDto> events = underTest.selectSqUpgradesByMostRecentFirst(dbSession, componentDto.uuid());
    assertThat(events).hasSize(2);
    assertThat(events.get(0).getUuid()).isEqualTo("E2");
    assertThat(events.get(0).getDate()).isEqualTo(newerDate);
    assertThat(events.get(1).getUuid()).isEqualTo("E1");
    assertThat(events.get(1).getDate()).isEqualTo(olderDate);
  }

  @Test
  void delete_by_uuid() {
    dbTester.events().insertEvent(newEvent(newAnalysis(ComponentTesting.newPrivateProjectDto())).setUuid("E1"));

    underTest.delete(dbTester.getSession(), "E1");
    dbTester.commit();

    assertThat(dbTester.countRowsOfTable("events")).isZero();
  }

}
