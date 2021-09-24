package com.octopus.http;

import io.vavr.control.Try;
import lombok.NonNull;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.inject.Singleton;

@Singleton
public class StringHttpClient implements HttpClient {
    public Try<String> get(@NonNull final String url) {
        return getClient()
            .of(httpClient -> getResponse(httpClient, url)
                .of(response -> EntityUtils.toString(response.getEntity()))
                    .get());
    }

    public Try.WithResources1<CloseableHttpClient> getClient() {
        return Try.withResources(HttpClients::createDefault);
    }

    public Try.WithResources1<CloseableHttpResponse> getResponse(
            @NonNull final CloseableHttpClient httpClient,
            @NonNull final String path) {
        return Try.withResources(() -> httpClient.execute(new HttpGet(path)));
    }
}
