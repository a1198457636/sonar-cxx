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
package org.sonar.cxx.checks.error;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static org.hamcrest.Matchers.containsString;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

public class ParsingErrorCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void test_syntax_error_recognition() throws UnsupportedEncodingException, IOException {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.ERROR_RECOVERY_ENABLED,
                    "false");

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/parsingError1.cc", ".");
    SourceFile file = CxxAstScanner
      .scanSingleInputFileConfig(tester.asInputFile(), squidConfig, new ParsingErrorCheck());

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(4).withMessageThat(containsString("Parse error"))
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void test_syntax_error_pperror() throws UnsupportedEncodingException, IOException {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.ERROR_RECOVERY_ENABLED,
                    "false");

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/parsingError2.cc", ".");
    SourceFile file = CxxAstScanner
      .scanSingleInputFileConfig(tester.asInputFile(), squidConfig, new ParsingErrorCheck());

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(2).withMessageThat(containsString("Parse error"))
      .noMore();
  }

}
