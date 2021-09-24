package com.octopus.builders.java;

import com.octopus.builders.PipelineBuilder;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.NonNull;

public class JavaBuilder implements PipelineBuilder {
    @Override
    public Boolean canBuild(@NonNull RepoAccessor accessor) {
        return null;
    }

    @Override
    public String generate(@NonNull RepoAccessor accessor) {
        return null;
    }
}
