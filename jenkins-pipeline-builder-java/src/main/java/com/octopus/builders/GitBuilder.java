package com.octopus.builders;

import com.google.common.collect.ImmutableList;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.dsl.StringContent;
import com.octopus.repoclients.RepoClient;
import java.util.List;
import lombok.NonNull;

/**
 * The base class containing common functions to build shared parts of the pipeline.
 */
public class GitBuilder {
  /**
   * Creates the comments that appear at the top of the pipeline.
   *
   * @return A list of Comment elements.
   */
  public List<Element> createTopComments() {
    return new ImmutableList.Builder<Element>()
        .add(Comment.builder()
            .content(
                "This pipeline requires the following plugins:\n* Pipeline Utility Steps Plugin: https://wiki.jenkins.io/display/JENKINS/Pipeline+Utility+Steps+Plugin")
            .build())
        .build();
  }

  /**
   * Displays some details about the environment.
   *
   * @return The stage element with a script displaying environment variables.
   */
  public Element createEnvironmentStage() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Environment")
        .children(createStepsElement(new ImmutableList.Builder<Element>()
            .add(StringContent.builder()
                .content("echo \"PATH = ${PATH}\"")
                .build())
            .build()))
        .build();
  }

  /**
   * Creates the steps to perform a git checkout.
   *
   * @param accessor The repo accessor defining the repo to checkout.
   * @return A list of Elements that build the checkout steps.
   */
  public Element createCheckoutStep(@NonNull final RepoClient accessor) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Checkout")
        .children(createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("checkout")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("$class", "GitSCM", ArgType.STRING))
                    .add(new Argument("branches", "[[name: '*/" + accessor.getDefaultBranches().get(0) + "']]", ArgType.ARRAY))
                    .add(new Argument("userRemoteConfigs",
                        "[[url: '" + accessor.getRepoPath() + "']]", ArgType.ARRAY))
                    .build())
                .build())
            .build()))
        .build();
  }

  /**
   * Creates a steps element holding the supplied children.
   *
   * @param children The child elements to place in the step.
   * @return A list with the single steps element.
   */
  public List<Element> createStepsElement(List<Element> children) {
    return new ImmutableList.Builder<Element>().add(
            FunctionTrailingLambda.builder()
                .name("steps")
                .children(children)
                .build())
        .build();

  }

  /**
   * Create the steps required to run gitversion and capture the results in environment vars.
   *
   * @return A list of steps executing and processing gitversion.
   */
  public List<Element> createGitVersionSteps() {
    return new ImmutableList.Builder<Element>()
        .add(Comment.builder()
            .content(
                "Gitversion is available from https://github.com/GitTools/GitVersion/releases.\n"
                    + "We attempt to run gitversion if the executable is available.")
            .build())
        .add(FunctionManyArgs.builder()
            .name("sh")
            .args(new ImmutableList.Builder<Argument>()
                .add(new Argument(
                    "script",
                    "which gitversion && gitversion /output buildserver || true",
                    ArgType.STRING))
                .build())
            .build())
        .add(Comment.builder()
            .content(
                "Capture the git version as an environment variable, or use a default version if gitversion wasn't available.\n"
                    + "https://gitversion.net/docs/reference/build-servers/jenkins")
            .build())
        .add(FunctionTrailingLambda.builder()
            .name("script")
            .children(new ImmutableList.Builder<Element>()
                .add(StringContent.builder()
                    .content(
                        "if (fileExists('gitversion.properties')) {\n"
                            + "  def props = readProperties file: 'gitversion.properties'\n"
                            + "  env.VERSION_SEMVER = props.GitVersion_SemVer\n"
                            + "  env.VERSION_BRANCHNAME = props.GitVersion_BranchName\n"
                            + "  env.VERSION_ASSEMBLYSEMVER = props.GitVersion_AssemblySemVer\n"
                            + "  env.VERSION_MAJORMINORPATCH = props.GitVersion_MajorMinorPatch\n"
                            + "  env.VERSION_SHA = props.GitVersion_Sha\n"
                            + "} else {\n"
                            + "  env.VERSION_SEMVER=1.0.0\n"
                            + "}"
                    )
                    .build())
                .build())
            .build())
        .build();
  }
}
