package com.octopus.repoaccessors;

public class GradleTestRepoAccessor extends TestRepoAccessor {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   */
  public GradleTestRepoAccessor(String repo, boolean findWrapper) {
    super(repo, findWrapper);
  }

  @Override
  public boolean testFile(String path) {
    if (path.equals("build.gradle") || path.equals("build.gradle.kts")) {
      return true;
    }

    if (findWrapper && path.equals("gradlew")) {
      return true;
    }

    return false;
  }
}
