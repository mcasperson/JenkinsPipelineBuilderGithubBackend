package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

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
  public boolean testFile(String path) {
    if (path.endsWith("pom.xml")) {
      return true;
    }

    if (findWrapper && path.endsWith("mvnw")) {
      return true;
    }

    return false;
  }
}
