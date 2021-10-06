package com.octopus.repoclients.github;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octopus.http.HttpClient;
import com.octopus.repoclients.RepoClient;
import io.vavr.control.Try;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.util.PatternMatcher;
import org.jboss.logging.Logger;

/**
 * An accessor that is configured to work with GitHub.
 */
@Builder
public class GithubRepoClient implements RepoClient {

  private static final Logger LOG = Logger.getLogger(GithubRepoClient.class.toString());
  private static final String GITHUB_REGEX = "https://github.com/(?<username>.*?)/(?<repo>.*?)(/|$)";
  private static final PatternMatcher ANT_PATH_MATCHER = new AntPathMatcher();
  private static final Pattern GITHUB_PATTERN = Pattern.compile(GITHUB_REGEX);
  private static final String GITHUB_CLIENT_ID_ENV_VAR = "GITHUB_CLIENT_ID";
  private static final String GITHUB_CLIENT_SECRET_ENV_VAR = "GITHUB_CLIENT_SECRET";

  @Getter
  @Setter
  private String repo;

  @Getter
  @Setter
  private HttpClient httpClient;

  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  private String password;

  @Override
  public Try<String> getFile(@NonNull final String path) {
    return getDetails()
        .flatMap(d -> getDefaultBranches().stream().map(b -> httpClient.get("https://raw.githubusercontent.com/" + d.getUsername() + "/" + d.getRepository() + "/" + b + "/" + path, username, password))
            .filter(Try::isSuccess)
            .findFirst()
            .orElse(Try.failure(new Exception("All attempts to find a file failed."))));
  }

  @Override
  public boolean testFile(@NonNull final String path) {
    return getDetails()
        .map(d -> getDefaultBranches()
            .stream()
            .map(b -> httpClient.head("https://raw.githubusercontent.com/" + d.getUsername() + "/" + d.getRepository() + "/" + b + "/" + path, username, password))
            .anyMatch(b -> b))
        .getOrElse(false);
  }

  @Override
  public Try<List<String>> getWildcardFiles(@NonNull final String path) {
    return getDetails()
        // Get the repository tree list
        .flatMap(d -> httpClient.get(
            "https://api.github.com/repos/" + d.getUsername() + "/" +  d.getRepository() + "/git/trees/master?recursive=1", username, password))
        // Convert the resulting JSON into a map
        .mapTry(j -> new ObjectMapper().readValue(j, Map.class))
        // files are contained in the tree array
        .mapTry(d -> (List<Map<Object, Object>>)d.get("tree"))
        // each member of the tree array is an object containing a path
        .mapTry(t -> t
            .stream()
            .map(u -> u.get("path").toString())
            .filter(p -> ANT_PATH_MATCHER.matches(path, p))
            .collect(Collectors.toList()));
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
  public List<String> getDefaultBranches() {
    return getDetails()
        // Get the repository details: https://docs.github.com/en/rest/reference/repos#get-a-repository
        .flatMap(d -> httpClient.get(
            "https://api.github.com/repos/" + d.getUsername() + "/" + d.getRepository()))
        // Convert the resulting JSON into a map
        .mapTry(j -> new ObjectMapper().readValue(j, Map.class))
        // get the default branch key
        .map(r -> r.get("default_branch"))
        // convert to a string
        .map(d -> List.of(d.toString()))
        // If there was a failure, assume the default branch is main or master.
        // We may also fall back to this if Github adds any rate limiting
        .getOrElse(List.of("main", "master"));
  }

  private Try<GithubRepoDetails> getDetails() {
    LOG.log(DEBUG, "GithubRepoAccessor.getDetails()");

    final Matcher matcher = GITHUB_PATTERN.matcher(repo);
    if (matcher.matches()) {
      final GithubRepoDetails retValue = new GithubRepoDetails(
          matcher.group("username"),
          matcher.group("repo"));

      LOG.log(DEBUG, "Found username: " + retValue.getUsername());
      LOG.log(DEBUG, "Found repo: " + retValue.getRepository());

      return Try.of(() -> retValue);
    }
    return Try.failure(new Exception("Failed to extract values from URL"));
  }
}
