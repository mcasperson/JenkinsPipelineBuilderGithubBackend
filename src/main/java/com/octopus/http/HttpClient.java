package com.octopus.http;


import io.vavr.control.Try;

public interface HttpClient {
    Try<String> get(String url);
}
