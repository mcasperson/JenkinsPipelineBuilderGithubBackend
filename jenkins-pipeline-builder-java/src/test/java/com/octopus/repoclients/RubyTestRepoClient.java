package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class RubyTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   */
  public RubyTestRepoClient(final String repo) {
    super(repo, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("Rubygem")) {
      return true;
    }

    return false;
  }
}
