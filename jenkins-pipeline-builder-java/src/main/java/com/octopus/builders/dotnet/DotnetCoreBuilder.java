package com.octopus.builders.dotnet;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.GitBuilder;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1Arg;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.repoclients.RepoClient;
import java.util.List;
import lombok.NonNull;

public class DotnetCoreBuilder implements PipelineBuilder {

  private static final GitBuilder GIT_BUILDER = new GitBuilder();
  private List<String> solutionFiles;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    this.solutionFiles = accessor.getWildcardFiles("*.sln").getOrElse(List.of());
    final List<String> projectFiles = accessor.getWildcardFiles("**/*.csproj").getOrElse(List.of());

    /*
     https://natemcmaster.com/blog/2017/03/09/vs2015-to-vs2017-upgrade/ provides some great insights
     into the various project file formats.
     */
    final boolean isDotNetCore = projectFiles.stream().anyMatch(f -> accessor.getFile(f).getOrElse("").matches("Sdk\\s*=\\s*\"Microsoft\\.NET\\.Sdk\""));
    return !solutionFiles.isEmpty() && isDotNetCore;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return FunctionTrailingLambda.builder()
        .name("pipeline")
        .children(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createTopComments())
            .add(Function1Arg.builder().name("agent").value("any").build())
            .add(FunctionTrailingLambda.builder()
                .name("stages")
                .children(new ImmutableList.Builder<Element>()
                    .add(GIT_BUILDER.createEnvironmentStage())
                    .add(GIT_BUILDER.createCheckoutStep(accessor))
                    .add(createDependenciesStep())
                    .add(createDependenciesInstallStep())
                    .add(createBuildStep())
                    .add(createTestStep())
                    .build())
                .build())
            .build()
        )
        .build()
        .toString();
  }

  private Element createDependenciesStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("List Dependencies")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(Comment.builder()
                .content(
                    "Save the dependencies that went into this build into an artifact. This allows you to review any builds for vulnerabilities later on.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "dotnet list package ${workspace}/" + solutionFiles.get(0) + " > dependencies.txt",
                        ArgType.STRING))
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

  private Element createDependenciesInstallStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Restore Dependencies")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(Comment.builder()
                .content(
                    "Save the dependencies that went into this build into an artifact. This allows you to review any builds for vulnerabilities later on.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "dotnet restore ${workspace}/" + solutionFiles.get(0),
                        ArgType.STRING))
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
                    .add(new Argument("script",
                        "dotnet build ${workspace}/" + solutionFiles.get(0) + " --configuration Release",
                        ArgType.STRING))
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
                    .add(new Argument("script", "dotnet test ${workspace}/" + solutionFiles.get(0),
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("step")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("$class", "NUnitPublisher", ArgType.STRING))
                    .add(new Argument("testResultsPattern", "**/TestResult.xml", ArgType.STRING))
                    .add(new Argument("debug", "false", ArgType.BOOLEAN))
                    .add(new Argument("keepJUnitReports", "true", ArgType.BOOLEAN))
                    .add(new Argument("skipJUnitArchiver", "false", ArgType.BOOLEAN))
                    .add(new Argument("failIfNoResults", "false", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }
}
