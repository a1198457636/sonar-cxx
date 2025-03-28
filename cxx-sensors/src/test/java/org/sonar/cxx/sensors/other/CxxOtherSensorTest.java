/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
package org.sonar.cxx.sensors.other;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxOtherSensorTest {

  @Rule
  public LogTester logTester = new LogTester();

  private CxxOtherSensor sensor;
  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldReportCorrectViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-ok.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    sensor = new CxxOtherSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(2);
  }

  @Test
  public void shouldReportFileLevelViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-filelevelviolation.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    sensor = new CxxOtherSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportProjectLevelViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-projectlevelviolation.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionWhenReportEmpty() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, false);
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-empty.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void shouldReportNoViolationsIfNoReportFound() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/noreport.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowInCaseOfATrashyReport() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, false);
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-invalid.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor();
    sensor.execute(context);
  }

  @Test
  public void shouldReportOnlyOneViolationAndRemoveDuplicates() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-with-duplicates.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    sensor = new CxxOtherSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    sensor = new CxxOtherSensor();
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX other analyser report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxOtherRepository.KEY);
    softly.assertAll();
  }

}
