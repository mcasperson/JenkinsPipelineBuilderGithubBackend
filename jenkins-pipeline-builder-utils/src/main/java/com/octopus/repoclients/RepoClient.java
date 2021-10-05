package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

/**
 * An abstraction for accessing files in a repo.
 */
public interface RepoClient {

  void setRepo(String repo);

  /**
   * Returns the contents of a file from the given path.
   *
   * @param path The repo file path
   * @return The file contents
   */
  Try<String> getFile(String path);

  boolean testFile(String path);

  String getRepoPath();

  List<String> getDefaultBranches();
}
