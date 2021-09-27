package com.octopus;

import com.octopus.builders.PipelineBuilder;
import com.octopus.http.HttpClient;
import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.repoaccessors.GithubRepoAccessor;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@Path("/pipeline")
public class PipelineResource {

    private static final PipelineBuilder[] BUILDERS = {new JavaMavenBuilder()};

    @Inject
    HttpClient httpClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("repo") final String repo) {
        if (StringUtils.isBlank(repo)) throw new IllegalArgumentException("repo can not be blank");

        final RepoAccessor accessor = new GithubRepoAccessor(repo, httpClient);

        return Arrays.stream(BUILDERS)
                .filter(b -> b.canBuild(accessor))
                .findFirst()
                .map(b -> b.generate(accessor))
                .orElse("No suitable builders were found.");
    }
}