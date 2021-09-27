package com.octopus;

import com.octopus.builders.PipelineBuilder;
import com.octopus.builders.java.JavaGradleBuilder;
import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.http.HttpClient;
import com.octopus.http.StringHttpClient;
import com.octopus.repoaccessors.RepoAccessor;
import com.octopus.repoaccessors.github.GithubRepoAccessor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class PipelineProducer {

  @ApplicationScoped
  @Produces
  public HttpClient getHttpClient() {
    return new StringHttpClient();
  }

  @RequestScoped
  @Produces
  public RepoAccessor getRepoAccessor(HttpClient httpClient) {
    return GithubRepoAccessor.builder()
        .httpClient(httpClient)
        .build();
  }

  @ApplicationScoped
  @Produces
  public PipelineBuilder getMavenBuilder() {
    return new JavaMavenBuilder();
  }

  @ApplicationScoped
  @Produces
  public PipelineBuilder getGradleBuilder() {
    return new JavaGradleBuilder();
  }
}
