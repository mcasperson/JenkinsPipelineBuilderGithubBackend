package com.octopus.builders.java;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.GitBuilder;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.*;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.NonNull;

import java.util.List;

public class JavaGradleBuilder implements PipelineBuilder {
    private static final GitBuilder GIT_BUILDER = new GitBuilder();
    private boolean usesWrapper = false;

    @Override
    public Boolean canBuild(@NonNull final RepoAccessor accessor) {
        if (GIT_BUILDER.fileExists(accessor, "build.gradle") || GIT_BUILDER.fileExists(accessor, "build.gradle.kts")) {
            usesWrapper = usesWrapper(accessor);
            return true;
        }

        return false;
    }

    @Override
    public String generate(@NonNull final RepoAccessor accessor) {
        return FunctionTrailingLambda.builder()
                .name("pipeline")
                .children(new ImmutableList.Builder<Element>()
                        .add(FunctionTrailingLambda.builder()
                                .name("tools")
                                .children(createTools())
                                .build())
                        .add(Function1Arg.builder().name("agent").value("any").build())
                        .add(FunctionTrailingLambda.builder()
                                .name("stages")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(GIT_BUILDER.createCheckoutStep(accessor))
                                        .add(createDependenciesStep())
                                        .add(createBuildStep())
                                        .add(createTestStep())
                                        .add(GIT_BUILDER.createDeployStep("build"))
                                        .build())
                                .build())
                        .build()
                )
                .build()
                .toString();
    }

    private Boolean usesWrapper(@NonNull final RepoAccessor accessor) {
        return GIT_BUILDER.fileExists(accessor, "gradlew");
    }

    private String gradleExecutable() {
        return usesWrapper ? "./gradlew" : "gradle";
    }

    private List<Element> createTools() {
        final ImmutableList.Builder<Element> list = new ImmutableList.Builder<Element>()
                .add(Function1Arg.builder().name("jdk").value("Java").build());

        if (!usesWrapper) {
            list.add(Function1Arg.builder().name("gradle").value("Gradle").build());
        }

        return list.build();
    }

    private Element createDependenciesStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("List Dependencies")
                .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
                        .add(Comment.builder()
                                .content("Save the dependencies that went into this build into an artifact. This allows you to review any builds for vulnerabilities later on.")
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", gradleExecutable() + " dependencies --console=plain > dependencies.txt", ArgType.STRING))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("archiveArtifacts")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("artifacts", "dependencies.txt", ArgType.STRING))
                                        .add(new Argument("fingerprint", "true", ArgType.BOOLEAN))
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    private Element createBuildStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Build")
                .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", gradleExecutable() + " assemble --console=plain", ArgType.STRING))
                                        .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    private Element createTestStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Test")
                .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", gradleExecutable() + " check --console=plain", ArgType.STRING))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("junit")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("", "build/test-results/**/*.xml", ArgType.STRING))
                                        .build())
                                .build())
                        .build()))
                .build();
    }
}
