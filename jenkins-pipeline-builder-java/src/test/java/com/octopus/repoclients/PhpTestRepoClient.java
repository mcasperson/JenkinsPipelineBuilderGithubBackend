package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class PhpTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   */
  public PhpTestRepoClient(final String repo) {
    super(repo, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("composer.json")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<List<String>> getWildcardFiles(String path) {
    return Try.of(() -> List.of());
  }
}
