package com.octopus.repoclients;

public class RubyTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public RubyTestRepoClient(final String repo, final String branch) {
    super(repo, branch, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("Gemfile")) {
      return true;
    }

    return false;
  }
}
