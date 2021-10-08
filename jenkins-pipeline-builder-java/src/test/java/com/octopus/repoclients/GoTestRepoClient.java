package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class GoTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   */
  public GoTestRepoClient(final String repo) {
    super(repo, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("go.mod")) {
      return true;
    }

    return false;
  }
}
