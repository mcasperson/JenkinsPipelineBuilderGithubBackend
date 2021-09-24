package com.octopus.repoaccessors;

import io.vavr.control.Try;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GithubRepoAccessor implements RepoAccessor {
    @Getter
    @Setter
    private String repo;

    @Getter
    @Setter
    private String accessToken;

    public GithubRepoAccessor(@NonNull final String repo) {
        this.repo = repo;
    }

    public GithubRepoAccessor(@NonNull final String repo, final String accessToken) {
        this.repo = repo;
        this.accessToken = accessToken;
    }

    @Override
    public String getFile(@NonNull final String path) {
        if (StringUtils.isBlank(accessToken)) {
            return getPublic(path);
        }

        return "";
    }

    private String getPublic(@NonNull final String path) {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet request = new HttpGet(ensureEndsWithSlash(getHttpPathFromRepo()) + path);
            try (final CloseableHttpResponse response = httpClient.execute(request)) {
                return Try.of(() -> EntityUtils.toString(response.getEntity()))
                        .getOrElse("");
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return "";
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
