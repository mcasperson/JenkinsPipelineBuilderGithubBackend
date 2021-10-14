package com.octopus.repoclients;

public class NodeTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public NodeTestRepoClient(final String repo) {
    super(repo, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("package.json")) {
      return true;
    }

    return false;
  }
}
