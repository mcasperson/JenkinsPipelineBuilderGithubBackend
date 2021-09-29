package com.octopus.http;

import io.vavr.control.Try;
import lombok.NonNull;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of HttpClient that returns the string content of any accessed file.
 */
public class StringHttpClient implements HttpClient {

  private static final Logger LOG = Logger.getLogger(StringHttpClient.class.toString());
  /**
   * A handy constant to change the logging this class produces. Should be FINE for production.
   */
  private static final Level LEVEL = Level.INFO;

  /**
   * Performs a HTTP GET request.
   *
   * @param url The URL to access.
   * @return A Try monad that either contains the String of the requested resource, or an exception
   */
  public Try<String> get(@NonNull final String url) {
    LOG.log(LEVEL, "StringHttpClient.get(String)");
    LOG.log(LEVEL, "url: " + url);

    return getClient()
        .of(httpClient -> getResponse(httpClient, url)
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
            .onSuccess(c -> LOG.log(LEVEL, "Request was successful."))
            .onFailure(e -> LOG.log(LEVEL, e.toString()));
  }

  /**
   * Performs a HTTP HEAD request.
   *
   * @param url The URL to access.
   * @return true if the request succeeded, and false otherwise.
   */
  public boolean head(@NonNull final String url) {
    LOG.log(LEVEL, "StringHttpClient.head(String)");
    LOG.log(LEVEL, "head: " + url);

    return getClient()
        .of(httpClient -> headResponse(httpClient, url).of(this::checkSuccess).get())
        .onSuccess(c -> LOG.log(LEVEL, "Request was successful."))
        .onFailure(e -> LOG.log(LEVEL, e.toString()))
        .isSuccess();
  }

  private Try.WithResources1<CloseableHttpClient> getClient() {
    return Try.withResources(HttpClients::createDefault);
  }

  private Try.WithResources1<CloseableHttpResponse> getResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path) {
    return Try.withResources(() -> httpClient.execute(new HttpGet(path)));
  }

  private Try.WithResources1<CloseableHttpResponse> headResponse(
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
