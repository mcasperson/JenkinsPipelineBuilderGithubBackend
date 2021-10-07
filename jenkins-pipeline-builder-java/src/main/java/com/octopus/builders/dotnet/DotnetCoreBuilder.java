package com.octopus.builders.dotnet;

import static org.jboss.logging.Logger.Level.DEBUG;

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
import com.octopus.dsl.StringContent;
import com.octopus.repoclients.RepoClient;
import java.util.List;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * The pipeline builder for dotnet core apps.
 */
public class DotnetCoreBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(DotnetCoreBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();
  private static final Pattern DOT_NET_CORE_REGEX = Pattern.compile(
      "Sdk\\s*=\\s*\"Microsoft\\.NET\\.Sdk\"");
  private List<String> solutionFiles;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "DotnetCoreBuilder.canBuild(RepoClient)");

    this.solutionFiles = accessor.getWildcardFiles("*.sln").getOrElse(List.of());
    final List<String> projectFiles = accessor.getWildcardFiles("**/*.csproj").getOrElse(List.of());

    LOG.log(DEBUG, "Found " + solutionFiles.size() + " solution files");
    solutionFiles.forEach(s -> LOG.log(DEBUG, "  " + s));

    LOG.log(DEBUG, "Found " + projectFiles.size() + " project files");
    projectFiles.forEach(s -> LOG.log(DEBUG, "  " + s));

    /*
     https://natemcmaster.com/blog/2017/03/09/vs2015-to-vs2017-upgrade/ provides some great insights
     into the various project file formats.
     */
    final boolean isDotNetCore = projectFiles
        .stream()
        .anyMatch(f -> DOT_NET_CORE_REGEX.matcher(accessor.getFile(f).getOrElse("")).find());

    LOG.log(DEBUG, "Project file were " + (isDotNetCore ? "" : "not ") + "DotNet Core projects");

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
                    .add(createDependenciesInstallStep())
                    .add(createDependenciesStep())
                    .add(createBuildStep())
                    .add(createTestStep())
                    .add(createPublishStep())
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
                        "dotnet list package > dependencies.txt",
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
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "dotnet restore",
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
                        "dotnet build --configuration Release",
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
                    .add(new Argument("script", "dotnet test -l:trx",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("mstest")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("testResultsFile", "**/*.trx", ArgType.STRING))
                    .add(new Argument("failOnError", "false", ArgType.BOOLEAN))
                    .add(new Argument("keepLongStdio", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createPublishStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Publish")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "dotnet publish --configuration Release",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content(
                            "// Find published DLL files.\n"
                                + "def files = findFiles(glob: '**/publish/*.dll')\n"
                                + "  .collect{it.path.substring(0, it.path.lastIndexOf(\"/\"))}\n"
                                + "  .unique(false)\n"
                                + "echo 'Found ' + files.size() + ' publish dirs'\n"
                                + "files.each{echo it}\n"
                                + "// Join the paths containing published application with colons.\n"
                                + "env.PUBLISH_PATHS = files.collect{it}.join(':')\n"
                                + "echo 'These paths are available from the PUBLISH_PATHS environment variable, separated by colons.'"
                        )
                        .build())
                    .build())
                .build())
            .add(Function1Arg.builder()
                .name("sh")
                .value("# Split the PUBLISH_PATHS variable on colons. Each segment represents a published application.\n"
                    + "export IFS=\":\"\n"
                    + "for PATH in ${PUBLISH_PATHS}; do\n"
                    + "  cd \"${WORKSPACE}/${PATH}\"\n"
                    + "  # Scan backwards for a csproj file. We'll use the project file name as the package ID.\n"
                    + "  for file in ../../../../*.csproj; do\n"
                    + "    [ -e \"$file\" ] && PACKAGEID=\"${file%.*}\" || PACKAGEID=\"application\"\n"
                    + "    break\n"
                    + "  done\n"
                    + "  # The Octopus CLI is used to create a package.\n"
                    + "  # Get the Octopus CLI from https://octopus.com/downloads/octopuscli#linux\n"
                    + "  /usr/bin/octo pack --id ${PACKAGEID} --format zip --include ** --version 1.0.0.${BUILD_NUMBER}\n"
                    + "  echo \"Created package \\\"${WORKSPACE}/${PATH}/${PACKAGEID}.1.0.0.${BUILD_NUMBER}.zip\\\"\"\n"
                    + "done")
                .build())
            .build()))
        .build();
  }
}
