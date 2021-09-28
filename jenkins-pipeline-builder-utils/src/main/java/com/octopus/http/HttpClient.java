package com.octopus.http;


import io.vavr.control.Try;

/**
 * Defines a read only HTTP client used to access files from git repos.
 */
public interface HttpClient {

  Try<String> get(String url);

  boolean head(String url);
}
