package com.octopus.builders.dotnet;

import com.octopus.builders.PipelineBuilder;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;

public class DotnetCoreBuilder implements PipelineBuilder {

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    return null;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return null;
  }
}
