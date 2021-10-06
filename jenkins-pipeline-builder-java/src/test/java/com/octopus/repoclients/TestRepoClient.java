package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public abstract class TestRepoClient implements RepoClient {

  private String repo;
  protected boolean findWrapper;

  /**
   * A mock repo accessor that pretends to find (or not find) project
   * files and wrapper scripts.
   * @param repo The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   *                    and false otherwise
   */
  public TestRepoClient(final String repo, boolean findWrapper) {
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
  public String getRepoPath() {
    return repo;
  }

  @Override
  public List<String> getDefaultBranches() {
    return List.of("main");
  }
}