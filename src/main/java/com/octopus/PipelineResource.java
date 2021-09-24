package com.octopus;

import com.octopus.http.HttpClient;
import com.octopus.builders.java.JavaBuilder;
import com.octopus.repoaccessors.GithubRepoAccessor;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/pipeline")
public class PipelineResource {

    @Inject
    private HttpClient httpClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("repo") final String repo) {
        if (StringUtils.isBlank(repo)) throw new IllegalArgumentException("repo can not be blank");

        return new JavaBuilder().generate(new GithubRepoAccessor(repo, httpClient));
    }
}