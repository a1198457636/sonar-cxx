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
package org.sonar.cxx.sensors.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.coverage.bullseye.CxxCoverageBullseyeSensor;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxBullseyeCoverageSensorTest {

  private static final Logger LOG = Loggers.get(CxxBullseyeCoverageSensorTest.class);
  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
  }

  @Test
  public void shouldReportCorrectCoverage() {
    var coverageReport = "coverage-reports/bullseye/coverage-result-bullseye.xml";
    var context = SensorContextTester.create(fs.baseDir());

    if (TestUtils.isWindows()) {
      settings.setProperty(CxxCoverageBullseyeSensor.REPORT_PATH_KEY, coverageReport);
      context.setSettings(settings);

      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "main.cpp")
        .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source_1.cpp")
        .setLanguage("cxx").initMetadata(
        "asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "src/testclass.h").setLanguage("cxx")
        .initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "src/testclass.cpp").setLanguage("cxx")
        .initMetadata(
          "asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "testclass.h").setLanguage("cxx").initMetadata(
        "asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "testclass.cpp").setLanguage("cxx")
        .initMetadata(
          "asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());

      var sensor = new CxxCoverageBullseyeSensor();
      sensor.execute(context);

      assertThat(context.lineHits("ProjectKey:main.cpp", 7)).isEqualTo(1);

      var zeroHitLines = new int[]{5, 10, 15, 17, 28, 32, 35, 40, 41, 44};
      for (var line : zeroHitLines) {
        LOG.debug("Check zero line coverage: {}", line);
        assertThat(context.lineHits("ProjectKey:source_1.cpp", line)).isZero();
      }

      var oneHitlinesA = new int[]{7, 12, 17, 30};
      for (var line : oneHitlinesA) {
        LOG.debug("Check line coverage: {}", line);
        assertThat(context.lineHits("ProjectKey:testclass.cpp", line)).isEqualTo(1);
        assertThat(context.lineHits("ProjectKey:src/testclass.cpp", line)).isEqualTo(1);
      }
      var fullCoveredTwoCondition = new int[]{34, 43, 46};
      for (var line : fullCoveredTwoCondition) {
        LOG.debug("Check full covered two conditions - line: {}", line);
        assertThat(context.conditions("ProjectKey:testclass.cpp", line)).isEqualTo(2);
        assertThat(context.conditions("ProjectKey:src/testclass.cpp", line)).isEqualTo(2);
        assertThat(context.coveredConditions("ProjectKey:testclass.cpp", line)).isEqualTo(2);
        assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", line)).isEqualTo(2);
        assertThat(context.lineHits("ProjectKey:testclass.cpp", line)).isEqualTo(1);
        assertThat(context.lineHits("ProjectKey:src/testclass.cpp", line)).isEqualTo(1);
      }

      LOG.debug("Check full covered four conditions - line: 42");
      assertThat(context.conditions("ProjectKey:testclass.cpp", 42)).isEqualTo(4);
      assertThat(context.conditions("ProjectKey:src/testclass.cpp", 42)).isEqualTo(4);
      assertThat(context.coveredConditions("ProjectKey:testclass.cpp", 42)).isEqualTo(4);
      assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", 42)).isEqualTo(4);
      assertThat(context.lineHits("ProjectKey:testclass.cpp", 42)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", 42)).isEqualTo(1);

      LOG.debug("Check partial covered two condition - line: 19");
      assertThat(context.conditions("ProjectKey:testclass.cpp", 19)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:testclass.cpp", 19)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:testclass.cpp", 19)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", 19)).isEqualTo(1);

      LOG.debug("Check full covered six conditions - line: 37");
      assertThat(context.conditions("ProjectKey:testclass.cpp", 37)).isEqualTo(6);
      assertThat(context.conditions("ProjectKey:src/testclass.cpp", 37)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:testclass.cpp", 37)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", 37)).isEqualTo(6);
      assertThat(context.lineHits("ProjectKey:testclass.cpp", 37)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", 37)).isEqualTo(1);
    }
  }

  @Test
  public void shouldParseTopLevelFiles() {
    // read top level folder name from report file
    var coverageReport = "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-win.xml";
    var context = SensorContextTester.create(fs.baseDir());
    if (TestUtils.isWindows()) {
      settings.setProperty(CxxCoverageBullseyeSensor.REPORT_PATH_KEY, coverageReport);
      context.setSettings(settings);

      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey",
                                                           "randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h")
        .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey",
                                                           "randomfoldernamethatihopeknowmachinehas/test/test.c")
        .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey",
                                                           "randomfoldernamethatihopeknowmachinehas/test2/test2.c")
        .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());
      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey",
                                                           "randomfoldernamethatihopeknowmachinehas/main.c")
        .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        .build());

      var sensor = new CxxCoverageBullseyeSensor();
      sensor.execute(context);

      assertThat(context.lineHits("ProjectKey:randomfoldernamethatihopeknowmachinehas/test/test.c", 4)).isEqualTo(1);
      assertThat(context.conditions("ProjectKey:randomfoldernamethatihopeknowmachinehas/test/test.c", 7)).isEqualTo(2);
    }
  }

  @Test
  public void shouldReportAllProbes() {

    var coverageReport = "coverage-reports/bullseye/bullseye-coverage-Linux-V8.9.60.xml";
    var context = SensorContextTester.create(fs.baseDir());

    if (TestUtils.isWindows()) {
      settings.setProperty(CxxCoverageBullseyeSensor.REPORT_PATH_KEY, coverageReport);
      context.setSettings(settings);

      var fileList = new String[]{
        "covfile/import/cereal/archives/json.hpp",
        "covfile/import/jpeg-compressor/src/jpgd.cpp",
        "covfile/src/main/vr_io/src/ModalityLUTJson.cpp",
        "covfile/src/test/vr_core/Image_unittest.cpp",
        "covfile/import/cereal/external/base64.hpp",
        "covfile/import/cereal/details/traits.hpp",
        "covfile/import/jpeg-compressor/src/jpge.cpp",
        "covfile/src/main/vr_io/src/RenderingParamJson.cpp",
        "covfile/import/cereal/external/rapidjson/prettywriter.h",
        "covfile/import/cereal/external/rapidjson/document.h",
        "covfile/src/main/main/main.cpp",
        "covfile/import/cereal/details/static_object.hpp",
        "covfile/src/main/vr_core/src/VR.cpp"
      };

      var sourceContent = new StringBuilder(8192);
      sourceContent.append("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
      for (var i = 0; i < 4000; i++) {
        sourceContent.append('\n');
      }

      for (var filepath : fileList) {
        context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", filepath)
          .setLanguage("cxx").initMetadata(sourceContent.toString()).build());
      }
      var sensor = new CxxCoverageBullseyeSensor();
      sensor.execute(context);

      var coveredCondition = new int[]{496, 524};

      for (var line : coveredCondition) {
        LOG.debug("Check conditions line: {}", line);
        assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", line)).isEqualTo(2);
        assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", line)).isEqualTo(2);
      }

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 530)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 530)).isEqualTo(5);

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 483)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 483)).isEqualTo(3);

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 552)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 552)).isEqualTo(1);

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 495)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 495)).isEqualTo(1);

      LOG.debug("Switch-label probe");
      assertThat(context.lineHits("ProjectKey:covfile/import/cereal/archives/json.hpp", 474)).isZero();
      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 474)).isEqualTo(1);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 474)).isZero();
      assertThat(context.lineHits("ProjectKey:covfile/import/cereal/archives/json.hpp", 475)).isEqualTo(1);
      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 475)).isEqualTo(1);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 475)).isEqualTo(1);

      LOG.debug("Try and catch probe on one line");
      assertThat(context.lineHits("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", 39)).isEqualTo(1);
      assertThat(context.conditions("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", 39)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", 39)).isEqualTo(1);
    }
  }

  @Test
  public void shouldIgnoreBlocks() {

    // report contains a block tag => ignore
    var coverageReport = "coverage-reports/bullseye/bullseye-coverage-Windows-V8.20.2.xml";
    var context = SensorContextTester.create(fs.baseDir());

    if (TestUtils.isWindows()) {
      settings.setProperty(CxxCoverageBullseyeSensor.REPORT_PATH_KEY, coverageReport);
      context.setSettings(settings);

      context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "root/folder/test.cpp")
        .setLanguage("cxx")
        .initMetadata(
          "namespace Core {\n"
            + "    class TokenHandler {\n"
            + "    public:\n"
            + "        virtual ~TokenHandler() {}\n"
            + "        virtual void OnHandle() = 0;\n"
            + "    };\n"
            + "}\n"
        )
        .build()
      );

      var sensor = new CxxCoverageBullseyeSensor();
      sensor.execute(context);

      assertThat(context.lineHits("ProjectKey:root/folder/test.cpp", 3)).isEqualTo(1);
    }
  }

}
