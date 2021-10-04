package com.octopus.repoaccessors;

import io.vavr.control.Try;
import java.util.List;

public class TestRepoAccessor implements RepoAccessor {

  private String repo;
  private boolean findWrapper;

  /**
   * A mock repo accessor that pretends to find (or not find) project
   * files and wrapper scripts.
   * @param repo The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   *                    and false otherwise
   */
  public TestRepoAccessor(final String repo, boolean findWrapper) {
    this.repo = repo;
    this.findWrapper = findWrapper;
  }

  @Override
  public void setRepo(final String repo) {
    this.repo = repo;
  }

  @Override
  public Try<String> getFile(String path) {
    return Try.of(() -> "");
  }

  @Override
  public boolean testFile(String path) {
    if (findWrapper) {
      return true;
    }

    return  !(path.endsWith("mvnw") || path.endsWith("gradlew"));
  }

  @Override
  public String getRepoPath() {
    return repo;
  }

  @Override
  public List<String> getDefaultBranches() {
    return List.of("main");
  }
}
