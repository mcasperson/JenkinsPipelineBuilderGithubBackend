package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class DotnetTestRepoClient extends TestRepoClient {

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public DotnetTestRepoClient(final String repo) {
    super(repo, false);
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith(".sln") || path.endsWith(".csproj")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<String> getFile(String path) {
    // just enough to fake a dotnet core project
    return Try.of(() -> "Sdk=\"Microsoft.NET.Sdk\"");
  }

  @Override
  public Try<List<String>> getWildcardFiles(String path) {
    return Try.of(() -> List.of("myproj.csproj"));
  }
}
