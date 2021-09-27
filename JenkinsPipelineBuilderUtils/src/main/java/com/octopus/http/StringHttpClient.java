package com.octopus.http;

import io.vavr.control.Try;
import lombok.NonNull;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class StringHttpClient implements HttpClient {

  public Try<String> get(@NonNull final String url) {
    return getClient()
        .of(httpClient -> getResponse(httpClient, url)
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get());
  }

  public boolean head(@NonNull final String url) {
    return getClient()
        .of(httpClient -> headResponse(httpClient, url).of(this::checkSuccess).get())
        .isSuccess();
  }

  public Try.WithResources1<CloseableHttpClient> getClient() {
    return Try.withResources(HttpClients::createDefault);
  }

  public Try.WithResources1<CloseableHttpResponse> getResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path) {
    return Try.withResources(() -> httpClient.execute(new HttpGet(path)));
  }

  public Try.WithResources1<CloseableHttpResponse> headResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path) {
    return Try.withResources(() -> httpClient.execute(new HttpHead(path)));
  }

  private CloseableHttpResponse checkSuccess(@NonNull final CloseableHttpResponse response)
      throws Exception {
    final int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code <= 399) {
      return response;
    }
    throw new Exception("Response did not indicate success");
  }
}
