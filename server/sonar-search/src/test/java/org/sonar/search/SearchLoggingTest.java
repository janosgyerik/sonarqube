/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.search;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.process.LogbackHelper;
import org.sonar.process.ProcessProperties;
import org.sonar.process.Props;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class SearchLoggingTest {
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private File logDir;

  private Props props = new Props(new Properties());
  private SearchLogging underTest = new SearchLogging();

  @Before
  public void setUp() throws IOException {
    logDir = temp.newFolder();
    props.set(ProcessProperties.PATH_LOGS, logDir.getAbsolutePath());
  }

  @AfterClass
  public static void resetLogback() throws Exception {
    new LogbackHelper().resetFromXml("/logback-test.xml");
  }

  @Test
  public void do_not_log_to_console() {
    LoggerContext ctx = underTest.configure(props);

    Logger root = ctx.getLogger(ROOT_LOGGER_NAME);
    Appender appender = root.getAppender("CONSOLE");
    assertThat(appender).isNull();
  }

  @Test
  public void log_to_es_file() {
    LoggerContext ctx = underTest.configure(props);

    Logger root = ctx.getLogger(ROOT_LOGGER_NAME);
    Appender<ILoggingEvent> appender = root.getAppender("file_es");
    assertThat(appender).isInstanceOf(FileAppender.class);
    FileAppender fileAppender = (FileAppender) appender;
    assertThat(fileAppender.getFile()).isEqualTo(new File(logDir, "es.log").getAbsolutePath());
    assertThat(fileAppender.getEncoder()).isInstanceOf(PatternLayoutEncoder.class);
    PatternLayoutEncoder encoder = (PatternLayoutEncoder) fileAppender.getEncoder();
    assertThat(encoder.getPattern()).isEqualTo("%d{yyyy.MM.dd HH:mm:ss} %-5level es[][%logger{20}] %msg%n");
  }

  @Test
  public void root_log_level_does_not_change_with_global_property_value() {
    props.set("sonar.log.level", "TRACE");

    LoggerContext ctx = underTest.configure(props);

    Logger rootLogger = ctx.getLogger(ROOT_LOGGER_NAME);
    assertThat(rootLogger.getLevel()).isEqualTo(Level.INFO);
  }

  @Test
  public void root_log_level_change_with_es_property_value() {
    props.set("sonar.log.level.es", "TRACE");

    LoggerContext ctx = underTest.configure(props);

    Logger rootLogger = ctx.getLogger(ROOT_LOGGER_NAME);
    assertThat(rootLogger.getLevel()).isEqualTo(Level.TRACE);
  }
}
