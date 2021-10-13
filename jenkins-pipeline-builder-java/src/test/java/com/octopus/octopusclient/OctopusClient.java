package com.octopus.octopusclient;

import com.octopus.http.StringHttpClient;
import java.util.List;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public class OctopusClient {

  private static final StringHttpClient STRING_HTTP_CLIENT = new StringHttpClient();
  private String apiKey;
  private String url;

  public OctopusClient() {

  }

  public OctopusClient(final String apiKey, final String url) {
    this.setApiKey(apiKey);
    this.setUrl(url);
  }

  public String getDefaultProjectGroupId() {
    return STRING_HTTP_CLIENT.get(getUrl() + "/api/Spaces-1/projectgroups/all",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .mapTry(j -> (List<Map<String, Object>>) new ObjectMapper().readValue(j, List.class))
        .mapTry(l -> l.stream()
            .filter(p -> p.get("Name").toString().equals("Default Project Group"))
            .map(p -> p.get("Id").toString())
            .findFirst()
            .get())
        .get();
  }

  public String getDefaultLifecycleId() {
    return STRING_HTTP_CLIENT.get(getUrl() + "/api/Spaces-1/lifecycles/all",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .mapTry(j -> (List<Map<String, Object>>) new ObjectMapper().readValue(j, List.class))
        .mapTry(l -> l.stream()
            .filter(p -> p.get("Name").toString().equals("Default Lifecycle"))
            .map(p -> p.get("Id").toString())
            .findFirst()
            .get())
        .get();
  }

  public String createEnvironment(final String name) {
    return STRING_HTTP_CLIENT.post(url + "/api/Spaces-1/environments",
            "{\"Name\": \"" + name + "\"}",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .get();
  }

  public String createProject(final String name, final String projectGroupId,
      final String lifecycleId) {
    return STRING_HTTP_CLIENT.post(url + "/api/Spaces-1/projects",
            "{\"Name\": \"Test\", \"ProjectGroupId\": \"" + projectGroupId + "\", \"LifeCycleId\": \""
                + lifecycleId + "\"}",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .get();
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
