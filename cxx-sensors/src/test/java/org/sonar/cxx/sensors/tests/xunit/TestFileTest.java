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
package org.sonar.cxx.sensors.tests.xunit;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFileTest {

  private TestFile testFile;

  @Before
  public void setUp() {
    testFile = new TestFile("test.cpp");
  }

  @Test
  public void newBornTestFileShouldHaveVirginStatistics() {
    assertEquals(0, testFile.getTests());
    assertEquals(0, testFile.getErrors());
    assertEquals(0, testFile.getFailures());
    assertEquals(0, testFile.getSkipped());
    assertEquals(0, testFile.getExecutionTime());
  }

  @Test
  public void addingTestCaseShouldIncrementStatistics() {
    int testBefore = testFile.getTests();
    long timeBefore = testFile.getExecutionTime();

    final int EXEC_TIME = 10;
    testFile.add(new TestCase("name", EXEC_TIME, "status", "stack", "msg",
                              "classname", "tcfilename", "tsname"));

    assertEquals(testFile.getTests(), testBefore + 1);
    assertEquals(testFile.getExecutionTime(), timeBefore + EXEC_TIME);
  }

  @Test
  public void addingAnErroneousTestCaseShouldIncrementErrorStatistic() {
    int errorsBefore = testFile.getErrors();
    TestCase error = mock(TestCase.class);
    when(error.isError()).thenReturn(true);

    testFile.add(error);

    assertEquals(testFile.getErrors(), errorsBefore + 1);
  }

  @Test
  public void addingAFailedTestCaseShouldIncrementFailedStatistic() {
    int failedBefore = testFile.getFailures();
    TestCase failedTC = mock(TestCase.class);
    when(failedTC.isFailure()).thenReturn(true);

    testFile.add(failedTC);

    assertEquals(testFile.getFailures(), failedBefore + 1);
  }

  @Test
  public void addingASkippedTestCaseShouldIncrementSkippedStatistic() {
    int skippedBefore = testFile.getSkipped();
    TestCase skippedTC = mock(TestCase.class);
    when(skippedTC.isSkipped()).thenReturn(true);

    testFile.add(skippedTC);

    assertEquals(testFile.getSkipped(), skippedBefore + 1);
  }

}
