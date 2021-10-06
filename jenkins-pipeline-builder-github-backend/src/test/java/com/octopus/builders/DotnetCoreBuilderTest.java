package com.octopus.builders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.builders.dotnet.DotnetCoreBuilder;
import com.octopus.http.StringHttpClient;
import com.octopus.repoclients.github.GithubRepoClient;
import org.junit.jupiter.api.Test;

public class DotnetCoreBuilderTest {
  private static final DotnetCoreBuilder DOTNET_CORE_BUILDER = new DotnetCoreBuilder();

  @Test
  public void verifyBuilderSupport() {
    assertTrue(DOTNET_CORE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .httpClient(new StringHttpClient())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .build()));
  }
}
