package com.octopus.repoaccessors;

public class GradleTestRepoAccessor extends TestRepoAccessor {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   */
  public GradleTestRepoAccessor(final String repo, final boolean findWrapper) {
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
