package com.octopus.repoaccessors;

import com.octopus.http.HttpClient;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class GithubRepoAccessor implements RepoAccessor {
    @Getter
    @Setter
    private String repo;

    @Getter
    @Setter
    private HttpClient httpClient;

    public GithubRepoAccessor(@NonNull final String repo, @NonNull final HttpClient httpClient) {
        this.repo = repo;
        this.httpClient = httpClient;
    }

    @Override
    public Try<String> getFile(@NonNull final String path) {
        return httpClient.get(ensureEndsWithSlash(getHttpPathFromRepo()) + path);
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
        if (!path.endsWith("/")) return path + "/";
        return path;
    }
}
