package com.octopus.builders.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.*;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.NonNull;

public class JavaBuilder implements PipelineBuilder {

    private final RepoAccessor accessor;

    public JavaBuilder (@NonNull final RepoAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public Boolean canBuild() {
        return accessor.getFile("pom.xml").isSuccess() ||
                accessor.getFile("build.gradle").isSuccess() ||
                accessor.getFile("build.gradle.kts").isSuccess();
    }

    @Override
    public String generate() {
        return FunctionTrailingLambda.builder()
                .name("pipeline")
                .children(new ImmutableList.Builder<Element>()
                        .add(FunctionTrailingLambda.builder()
                                .name("tools")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(Function1Arg.builder().name("maven").value("Maven 3").build())
                                        .add(Function1Arg.builder().name("jdk").value("Java").build())
                                        .build()
                                )
                                .build())
                        .add(Function1Arg.builder().name("agent").value("any").build())
                        .add(FunctionTrailingLambda.builder()
                                .name("stages")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(createBuildStep())
                                        .add(Function1ArgTrailingLambda.builder()
                                                .name("stage")
                                                .arg("Test")
                                                .build())
                                        .add(Function1ArgTrailingLambda.builder()
                                                .name("stage")
                                                .arg("Package")
                                                .build())
                                        .add(Function1ArgTrailingLambda.builder()
                                                .name("stage")
                                                .arg("Deploy")
                                                .build())
                                        .build())
                                .build())
                        .build()
                )
                .build()
                .toString();
    }

    private Element createBuildStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Build")
                .children(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode versions:set -DnewVersion=1.0.${BUILD_NUMBER}", ArgType.STRING))
                                        .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode package -DskipTests", ArgType.STRING))
                                        .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
