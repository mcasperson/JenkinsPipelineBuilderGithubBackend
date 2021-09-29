package com.octopus.lambda;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the AWS Lambda proxy response.
 */
public class ProxyResponse {
  public final String statusCode;
  public final String body;
  public final Map<String, String> headers;

  public ProxyResponse(
      final String statusCode,
      final String body,
      final Map<String, String> headers) {
    this.headers = headers == null ? new HashMap<>() : headers;
    this.body = body;
    this.statusCode = statusCode;
    addCORSHeaders();
  }

  public ProxyResponse(
      final String statusCode,
      final String body) {
    this.headers = new HashMap<>();
    this.body = body;
    this.statusCode = statusCode;
    addCORSHeaders();
  }

  private void addCORSHeaders() {
    headers.put("Access-Control-Allow-Origin", "*");
  }
}