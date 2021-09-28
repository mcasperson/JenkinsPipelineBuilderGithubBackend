package com.octopus.producer;

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

  /**
   * Produces the HTTP client.
   *
   * @return An implementation of HttpClient.
   */
  @ApplicationScoped
  @Produces
  public HttpClient getHttpClient() {
    return new StringHttpClient();
  }

  /**
   * Produces the repository accessor.
   *
   * @return An implementation of RepoAccessor.
   */
  @RequestScoped
  @Produces
  public RepoAccessor getRepoAccessor(HttpClient httpClient) {
    return GithubRepoAccessor.builder()
        .httpClient(httpClient)
        .build();
  }

  /**
   * Produces the Maven pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getMavenBuilder() {
    return new JavaMavenBuilder();
  }

  /**
   * Produces the Gradle pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getGradleBuilder() {
    return new JavaGradleBuilder();
  }
}
