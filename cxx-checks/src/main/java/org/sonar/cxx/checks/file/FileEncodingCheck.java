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
package org.sonar.cxx.checks.file;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.NoSqale;
import org.sonar.cxx.squidbridge.checks.SquidCheck;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;

/**
 * FileEncodingCheck
 */
@Rule(
  key = "FileEncoding",
  name = "Verify that all characters of the file can be encoded with the predefined charset.",
  priority = Priority.MINOR)
@ActivatedByDefault
@NoSqale
public class FileEncodingCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private Charset charset = StandardCharsets.UTF_8;

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode astNode) {
    try {
      Files.readAllLines(getContext().getInputFile().file().toPath(), charset);
    } catch (IOException e) {
      getContext().createFileViolation(this,
                                       "Not all characters of the file can be encoded with the predefined charset "
                                         + charset.name() + ".");
    }
  }

}
