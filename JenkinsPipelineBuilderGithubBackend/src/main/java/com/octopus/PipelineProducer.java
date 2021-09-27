package com.octopus;

import com.octopus.http.HttpClient;
import com.octopus.http.StringHttpClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class PipelineProducer {
    @Produces
    public HttpClient getHttpClient() {
        return new StringHttpClient();
    }
}
