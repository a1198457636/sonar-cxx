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
package org.sonar.cxx.lexer;

import org.sonar.cxx.parser.CxxLexer;
import com.sonar.sslr.impl.Lexer;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.cxx.parser.CxxTokenType;
import static org.sonar.cxx.lexer.LexerAssert.assertThat;

public class CxxLexer_PreprocessorDisabled_Test {

  private static Lexer lexer;

  @BeforeClass
  public static void init() {
    lexer = CxxLexer.create();
  }

  @Test
  public void preprocessor_directives() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("#include <iostream>")).anySatisfy(token -> assertThat(token).isValue(
      "#include <iostream>").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("# include <iostream>")).anySatisfy(token -> assertThat(token).isValue(
      "# include <iostream>").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex(" # include <iostream>")).anySatisfy(token -> assertThat(token).isValue(
      "# include <iostream>").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define lala")).anySatisfy(token -> assertThat(token).isValue("#define lala").hasType(
      CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("# define lala")).anySatisfy(token -> assertThat(token).isValue("# define lala")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex(" # define lala")).anySatisfy(token -> assertThat(token).isValue("# define lala")
      .hasType(CxxTokenType.PREPROCESSOR));

    softly.assertThat(lexer.lex("#include <iostream>")).hasSize(2);
    softly.assertThat(lexer.lex("#define\\\ncontinued line")).hasSize(2);
    softly.assertThat(lexer.lex("#include <iostream>\n1")).anySatisfy(token -> assertThat(token).isValue("1").hasType(
      CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void preprocessor_continued_define() {
    assertThat(lexer.lex("#define M\\\n"
                           + "0")).anySatisfy(token -> assertThat(token).isValue("#define M 0").hasType(
      CxxTokenType.PREPROCESSOR));
  }

  @Test
  public void preprocessor_directive_with_comment() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("#define A B*/\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A B*/")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define A B/*CCC*/\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A B")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define A B/**/C\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A BC")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define A B/*C\n\n\nC*/D\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A BD").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define A \"a/*\" B\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A \"a/*\" B").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define A \"-str/*\"-/*CCC*/\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A \"-str/*\"-").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define A B/*-\"str\"-*/C\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A BC").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define A B//-/*-\"str\"-*/\n")).anySatisfy(token -> assertThat(token)
      .isValue("#define A B").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertAll();
  }

}
