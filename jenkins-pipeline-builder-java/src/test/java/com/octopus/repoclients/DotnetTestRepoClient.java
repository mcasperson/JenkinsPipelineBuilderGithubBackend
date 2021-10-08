package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class DotnetTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   */
  public DotnetTestRepoClient(final String repo) {
    super(repo, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith(".sln")) {
      return true;
    }

    return false;
  }
}
