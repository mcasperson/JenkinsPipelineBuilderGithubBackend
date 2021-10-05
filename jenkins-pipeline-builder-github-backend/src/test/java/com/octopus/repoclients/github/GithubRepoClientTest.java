package com.octopus.repoclients.github;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.http.HttpClient;
import com.octopus.http.StringHttpClient;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.Test;


public class GithubRepoClientTest {
  private static final HttpClient HTTP_CLIENT = new StringHttpClient();

  @Test
  public void testRepoScanning() {
    final Try<List<String>> files = GithubRepoClient.builder()
        .httpClient(HTTP_CLIENT)
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .build()
        .getWildcardFiles("*.sln");

    assertTrue(files.isSuccess());
    assertTrue(files.get().contains("RandomQuotes.sln"));
  }
}
