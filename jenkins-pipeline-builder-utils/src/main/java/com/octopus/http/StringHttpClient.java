package com.octopus.http;

import static org.jboss.logging.Logger.Level.DEBUG;

import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.jboss.logging.Logger;
import lombok.NonNull;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * An implementation of HttpClient that returns the string content of any accessed file.
 */
public class StringHttpClient implements HttpClient {

  private static final Logger LOG = Logger.getLogger(StringHttpClient.class.toString());

  /**
   * Performs a HTTP GET request.
   *
   * @param url The URL to access.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> get(@NonNull final String url) {
    LOG.log(DEBUG, "StringHttpClient.get(String)");
    LOG.log(DEBUG, "url: " + url);

    return getClient()
        .of(httpClient -> getResponse(httpClient, url, List.of())
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
  }

  @Override
  public Try<String> get(
      @NonNull final String url,
      final String username,
      final String password) {
    LOG.log(DEBUG, "StringHttpClient.get(String, String, String)");
    LOG.log(DEBUG, "head: " + url);
    LOG.log(DEBUG, "username: " + username);

    return getClient()
        .of(httpClient -> getResponse(
              httpClient, url,
              buildHeaders(username, password))
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
  }

  /**
   * Performs a HTTP HEAD request.
   *
   * @param url The URL to access.
   * @return true if the request succeeded, and false otherwise.
   */
  public boolean head(@NonNull final String url) {
    LOG.log(DEBUG, "StringHttpClient.head(String)");
    LOG.log(DEBUG, "head: " + url);

    return getClient()
        .of(httpClient -> headResponse(httpClient, url, List.of()).of(this::checkSuccess).get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()))
        .isSuccess();
  }

  @Override
  public boolean head(
      @NonNull final String url,
      final String username,
      final String password) {
    LOG.log(DEBUG, "StringHttpClient.head(String, String, String)");
    LOG.log(DEBUG, "head: " + url);
    LOG.log(DEBUG, "username: " + username);

    return getClient()
        .of(httpClient -> headResponse(
              httpClient, url,
              buildHeaders(username, password))
            .of(this::checkSuccess).get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()))
        .isSuccess();
  }

  private List<Header> buildHeaders(final String username, final String password) {
    return Stream.of(buildAuthHeader(username, password))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<BasicHeader> buildAuthHeader(final String username, final String password) {
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      return Optional.empty();
    }

    return Optional.of(new BasicHeader(
          "AUTHORIZATION",
          "Basic " + Base64.encodeBase64((username + ":" + password).getBytes())));
  }

  private Try.WithResources1<CloseableHttpClient> getClient() {
    return Try.withResources(HttpClients::createDefault);
  }

  private Try.WithResources1<CloseableHttpResponse> getResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    return Try.withResources(() -> httpClient.execute(getRequest(path, headers)));
  }

  private Try.WithResources1<CloseableHttpResponse> headResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    return Try.withResources(() -> httpClient.execute(headRequest(path, headers)));
  }

  private HttpRequestBase headRequest(
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    final HttpRequestBase request = new HttpHead(path);
    headers.forEach(request::addHeader);
    return request;
  }

  private HttpRequestBase getRequest(
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    final HttpRequestBase request = new HttpGet(path);
    headers.forEach(request::addHeader);
    return request;
  }

  private CloseableHttpResponse checkSuccess(@NonNull final CloseableHttpResponse response)
      throws Exception {
    LOG.log(DEBUG, "StringHttpClient.checkSuccess(CloseableHttpResponse)");

    final int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code <= 399) {
      LOG.log(DEBUG, "Response code " + code + " indicated success");
      return response;
    }

    LOG.log(DEBUG, "Response code " + code + " did not indicate success");
    throw new Exception("Response did not indicate success");
  }
}
