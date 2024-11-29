/*
 * SonarLint Core - Analysis Engine
 * Copyright (C) 2016-2024 SonarSource SA
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
package testutils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.utils.PathUtils;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.analysis.container.analysis.filesystem.FileMetadata;
import org.sonarsource.sonarlint.core.analysis.container.analysis.filesystem.SonarLintInputFile;
import org.sonarsource.sonarlint.core.commons.Language;

/**
 * Intended to be used in unit tests that need to create {@link InputFile}s.
 * An InputFile is unambiguously identified by a <b>module key</b> and a <b>relative path</b>, so these parameters are mandatory.
 * <p>
 * A module base directory is only needed to construct absolute paths.
 * <p>
 * Examples of usage of the constructors:
 *
 * <pre>
 * InputFile file1 = TestInputFileBuilder.create("module1", "myfile.java").build();
 * InputFile file2 = TestInputFileBuilder.create("", fs.baseDir(), myfile).build();
 * </pre>
 * <p>
 * file1 will have the "module1" as both module key and module base directory.
 * file2 has an empty string as module key, and a relative path which is the path from the filesystem base directory to myfile.
 */
public class TestInputFileBuilder {
  private final String relativePath;
  @CheckForNull
  private Path baseDir;
  private Language language;
  private InputFile.Type type = InputFile.Type.MAIN;
  private int lines = -1;
  private int[] originalLineStartOffsets = new int[0];
  private int lastValidOffset = -1;

  public TestInputFileBuilder(String relativePath) {
    this.relativePath = PathUtils.sanitize(relativePath);
  }

  public TestInputFileBuilder setBaseDir(Path baseDir) {
    this.baseDir = baseDir;
    return this;
  }

  public TestInputFileBuilder setLanguage(@Nullable Language language) {
    this.language = language;
    return this;
  }

  public TestInputFileBuilder setType(InputFile.Type type) {
    this.type = type;
    return this;
  }

  public TestInputFileBuilder setLines(int lines) {
    this.lines = lines;
    return this;
  }

  public TestInputFileBuilder setLastValidOffset(int lastValidOffset) {
    this.lastValidOffset = lastValidOffset;
    return this;
  }

  public TestInputFileBuilder setOriginalLineStartOffsets(int[] originalLineStartOffsets) {
    this.originalLineStartOffsets = originalLineStartOffsets;
    return this;
  }

  public TestInputFileBuilder setMetadata(FileMetadata.Metadata metadata) {
    this.setLines(metadata.lines());
    this.setLastValidOffset(metadata.lastValidOffset());
    this.setOriginalLineStartOffsets(metadata.originalLineOffsets());
    return this;
  }

  public TestInputFileBuilder initMetadata(String content) {
    return setMetadata(
      new FileMetadata().readMetadata(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8, URI.create("file://test"), null));
  }

  public SonarLintInputFile build() {
    ClientInputFile clientInputFile = new InMemoryTestClientInputFile("contents", relativePath, baseDir != null ? baseDir.resolve(relativePath) : null, type == Type.TEST,
      language);
    return new SonarLintInputFile(clientInputFile, f -> new FileMetadata.Metadata(lines, originalLineStartOffsets, lastValidOffset))
      .setType(type)
      .setLanguage(language);
  }
}
