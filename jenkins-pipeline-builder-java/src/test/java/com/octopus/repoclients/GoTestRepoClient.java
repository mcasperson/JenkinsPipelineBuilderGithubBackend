package com.octopus.repoclients;

public class GoTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public GoTestRepoClient(final String repo, final String branch) {
    super(repo, branch, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("go.mod")) {
      return true;
    }

    return false;
  }
}
