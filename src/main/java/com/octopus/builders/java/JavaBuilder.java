package com.octopus.builders.java;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.Element;
import com.octopus.dsl.ParentElement;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.NonNull;

public class JavaBuilder implements PipelineBuilder {
    @Override
    public Boolean canBuild(@NonNull final RepoAccessor accessor) {
        return accessor.getFile("pom.xml").isSuccess() ||
                accessor.getFile("build.gradle").isSuccess() ||
                accessor.getFile("build.gradle.kts").isSuccess();
    }

    @Override
    public String generate(@NonNull final RepoAccessor accessor) {
        return ParentElement.builder()
                .name("pipeline")
                .children(new ImmutableList.Builder<Element>()
                        .add(ParentElement.builder().name("tools").build())
                        .add(ParentElement.builder().name("stages").build())
                        .build()
                )
                .build()
                .toString();
    }
}
