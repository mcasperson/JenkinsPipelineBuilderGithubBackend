package com.octopus.http;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StringHttpClientTest {
  @Test
  public void testStringHttpClient() {
    final HttpClient httpClient = new StringHttpClient();
    assertTrue(httpClient.get("https://google.com").isSuccess());
    assertTrue(httpClient.head("https://google.com"));
  }
}
