package com.octopus;

import com.octopus.builders.java.JavaBuilder;
import com.octopus.repoaccessors.GithubRepoAccessor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/pipeline")
public class PipelineResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("repo") final String repo) {
       return new JavaBuilder()
               .generate(new GithubRepoAccessor(repo));
    }
}