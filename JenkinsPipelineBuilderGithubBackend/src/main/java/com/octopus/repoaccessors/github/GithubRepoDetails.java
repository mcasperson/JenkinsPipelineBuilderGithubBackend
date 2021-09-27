package com.octopus.repoaccessors.github;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GithubRepoDetails {

  private String username;
  private String repository;
}
