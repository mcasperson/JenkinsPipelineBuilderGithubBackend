package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.octopus.builders.PipelineBuilder;
import com.octopus.repoaccessors.RepoAccessor;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

/**
 * The AWS Lambda server.
 */
public class PipelineLambda implements RequestHandler<Map<String,Object>, ProxyResponse> {
    @Inject
    RepoAccessor accessor;

    @Inject
    Instance<PipelineBuilder> builders;

    /**
     * The Lambda entry point.
     * @param input The JSON object passed in. This is expected to be formatted using proxy integration.
     * @param context The Lambda context.
     * @return The Lambda proxy integration response.
     */
    @Override
    public ProxyResponse handleRequest(final Map<String,Object> input, final Context context) {
        final String repo = Optional
            .ofNullable(input.getOrDefault("queryStringParameters", null))
            .map(Map.class::cast)
            .map(m -> m.getOrDefault("repo", null))
            .map(Object::toString)
            .orElse("");

        if (StringUtils.isBlank(repo)) {
            throw new IllegalArgumentException("repo can not be blank");
        }

        accessor.setRepo(repo);

        final String pipeline = builders.stream()
            .filter(b -> b.canBuild(accessor))
            .findFirst()
            .map(b -> b.generate(accessor))
            .orElse("No suitable builders were found.");

        return new ProxyResponse("200", pipeline);
    }
}
