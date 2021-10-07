package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class PythonTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   */
  public PythonTestRepoClient(final String repo) {
    super(repo, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("requirements.txt")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<List<String>> getWildcardFiles(String path) {
    return Try.failure(new Exception("Not implemented"));
  }
}
