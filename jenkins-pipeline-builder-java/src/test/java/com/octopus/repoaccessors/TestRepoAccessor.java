package com.octopus.repoaccessors;

import io.vavr.control.Try;
import java.util.List;

public class TestRepoAccessor implements RepoAccessor {

  private String repo;

  public TestRepoAccessor(final String repo) {
    setRepo(repo);
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
    return true;
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
