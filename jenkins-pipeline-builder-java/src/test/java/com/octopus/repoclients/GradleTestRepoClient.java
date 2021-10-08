package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class GradleTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   */
  public GradleTestRepoClient(final String repo, final boolean findWrapper) {
    super(repo, findWrapper);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("build.gradle") || path.endsWith("build.gradle.kts")) {
      return true;
    }

    if (findWrapper && path.endsWith("gradlew")) {
      return true;
    }

    return false;
  }
}
