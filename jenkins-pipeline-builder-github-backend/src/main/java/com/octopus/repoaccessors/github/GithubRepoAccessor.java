package com.octopus.repoaccessors.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octopus.Config;
import com.octopus.http.HttpClient;
import com.octopus.repoaccessors.RepoAccessor;
import io.vavr.control.Try;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jboss.logging.Logger;

/**
 * An accessor that is configured to work with GitHub.
 */
@Builder
public class GithubRepoAccessor implements RepoAccessor {

  private static final Logger LOG = Logger.getLogger(GithubRepoAccessor.class.toString());
  private static final String GITHUB_REGEX = "https://github.com/(?<username>.*?)/(?<repo>.*?)(/|$)";
  private static final Pattern GITHUB_PATTERN = Pattern.compile(GITHUB_REGEX);

  @Getter
  @Setter
  private String repo;

  @Getter
  @Setter
  private HttpClient httpClient;

  @Override
  public Try<String> getFile(@NonNull final String path) {
    return httpClient.get(ensureEndsWithSlash(getHttpPathFromRepo()) + path);
  }

  @Override
  public boolean testFile(@NonNull final String path) {
    return httpClient.head(ensureEndsWithSlash(getHttpPathFromRepo()) + path);
  }

  @Override
  public String getRepoPath() {
    if (!repo.endsWith(".git")) {
      return repo + ".git";
    }
    return repo;
  }

  private String getHttpPathFromRepo() {
    if (repo.endsWith(".git")) {
      return repo.substring(0, repo.length() - 4);
    }
    return repo;
  }

  private String ensureEndsWithSlash(@NonNull final String path) {
    if (!path.endsWith("/")) {
      return path + "/";
    }
    return path;
  }

  /**
   * Returns the default branch for a GitHub repo.
   *
   * @return The repository default branch.
   */
  public String getDefaultBranch() {
    return getDetails()
        // Get the repository details: https://docs.github.com/en/rest/reference/repos#get-a-repository
        .map(d -> httpClient.get(
            "https://api.github.com/repos/" + d.getUsername() + "/" + d.getRepository()))
        // Convert the resulting JSON into a map
        .map(t -> t.mapTry(j -> new ObjectMapper().readValue(j, Map.class)))
        // If there was a failure, return null, so Optional will stop chaining
        .map(t -> t.getOrElse(() -> null))
        // get the default branch key
        .map(r -> r.get("default_branch"))
        // convert to a string
        .map(Object::toString)
        // If there was a failure, assume the default branch is main
        .orElse("main");
  }

  private Optional<GithubRepoDetails> getDetails() {
    LOG.log(Config.LEVEL, "GithubRepoAccessor.getDetails()");

    final Matcher matcher = GITHUB_PATTERN.matcher(repo);
    if (matcher.matches()) {
      final GithubRepoDetails retValue = new GithubRepoDetails(
          matcher.group("username"),
          matcher.group("repo"));

      LOG.log(Config.LEVEL, "Found username: " + retValue.getUsername());
      LOG.log(Config.LEVEL, "Found repo: " + retValue.getRepository());

      return Optional.of(retValue);
    }
    return Optional.empty();
  }
}
