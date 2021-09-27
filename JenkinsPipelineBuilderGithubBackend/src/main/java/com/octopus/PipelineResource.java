package com.octopus;

import com.octopus.builders.PipelineBuilder;
import com.octopus.repoaccessors.RepoAccessor;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/pipeline")
public class PipelineResource {

    @Inject
    RepoAccessor accessor;

    @Inject
    Instance<PipelineBuilder> builders;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("repo") final String repo) {
        if (StringUtils.isBlank(repo)) throw new IllegalArgumentException("repo can not be blank");

        accessor.setRepo(repo);

        return builders.stream()
                .filter(b -> b.canBuild(accessor))
                .findFirst()
                .map(b -> b.generate(accessor))
                .orElse("No suitable builders were found.");
    }
}