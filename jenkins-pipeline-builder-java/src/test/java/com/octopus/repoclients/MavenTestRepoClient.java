package com.octopus.repoclients;

public class MavenTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   */
  public MavenTestRepoClient(final String repo, final boolean findWrapper) {
    super(repo, findWrapper);
  }

  @Override
  public boolean testFile(final String path) {
    if (path.endsWith("pom.xml")) {
      return true;
    }

    if (findWrapper && path.endsWith("mvnw")) {
      return true;
    }

    return false;
  }
}
