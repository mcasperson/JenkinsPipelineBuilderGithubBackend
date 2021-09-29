package com.octopus.resource;

import com.octopus.builders.PipelineBuilder;
import com.octopus.repoaccessors.RepoAccessor;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

/**
 * The REST server.
 */
@Path("/pipeline")
public class PipelineResource {

  @Inject
  RepoAccessor accessor;

  @Inject
  Instance<PipelineBuilder> builders;

  /**
   * Generates a Jenkins pipeline from the given git repository.
   *
   * @param repo The repository URL
   * @return The Jenkins pipeline.
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello(@QueryParam("repo") final String repo) {
    if (StringUtils.isBlank(repo)) {
      throw new IllegalArgumentException("repo can not be blank");
    }
    accessor.setRepo(repo);

    return builders.stream()
        .filter(b -> b.canBuild(accessor))
        .findFirst()
        .map(b -> b.generate(accessor))
        .orElse("No suitable builders were found.");

  }
}