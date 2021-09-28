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
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class PipelineLambda implements RequestHandler<Map<String,Object>, ProxyResponse> {
    @Inject
    RepoAccessor accessor;

    @Inject
    Instance<PipelineBuilder> builders;

    @Override
    public ProxyResponse handleRequest(final Map<String,Object> input, Context context) {
        final String repo = Optional
            .ofNullable(input.getOrDefault("queryStringParameters", null))
            .map(Map.class::cast)
            .map(m -> m.getOrDefault("repo", null))
            .map(Object::toString)
            .orElseGet(null);

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
