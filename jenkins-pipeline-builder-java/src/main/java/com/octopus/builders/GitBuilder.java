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
                "This pipeline requires the Pipeline Utility Steps Plugin: https://wiki.jenkins.io/display/JENKINS/Pipeline+Utility+Steps+Plugin")
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
}
