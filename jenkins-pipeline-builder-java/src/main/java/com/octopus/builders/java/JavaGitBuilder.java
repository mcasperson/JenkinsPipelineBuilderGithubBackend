package com.octopus.builders.java;

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
import java.util.logging.Logger;
import lombok.NonNull;

/**
 * A utility class containing useful methods common to Java pipelines.
 */
public class JavaGitBuilder {

  private static final Logger LOG = Logger.getLogger(JavaGitBuilder.class.toString());

  /**
   * Tests to see if a file exists.
   *
   * @param accessor The repo accessor.
   * @param file     The file to test.
   * @return true if the file exists, and false otherwise.
   */
  public boolean fileExists(@NonNull final RepoClient accessor, @NonNull final String file) {
    return accessor.getDefaultBranches()
        .stream()
        .anyMatch(b -> accessor.testFile("blob/" + b + "/" + file));
  }

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

  /**
   * Creates the deployment stage.
   *
   * @param buildDir The directory holding the compiled artifacts.
   * @return A list with a single element representing the stage.
   */
  public Element createDeployStep(@NonNull final String buildDir) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Deploy")
        .children(createStepsElement(new ImmutableList.Builder<Element>()
            .add(Comment.builder()
                .content(
                    "This scans through the build tool output directory and find the largest file, "
                        + "which we assume is the artifact that was intended to be deployed.\n"
                        + "The path to this file is saved in and environment variable called "
                        + "JAVA_ARTIFACT, which can be consumed by subsequent custom deployment "
                        + "steps.")
                .build())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content(
                            "// Find the matching artifacts\n"
                                + "def extensions = ['jar', 'war']\n"
                                + "def files = []\n"
                                + "for(extension in extensions){\n"
                                + "    findFiles(glob: '" + buildDir
                                + "/**.' + extension).each{files << it}\n"
                                + "}\n"
                                + "echo 'Found ' + files.size() + ' potential artifacts'\n"
                                + "// Assume the largest file is the artifact we intend to deploy\n"
                                + "def largestFile = null\n"
                                + "for (i = 0; i < files.size(); ++i) {\n"
                                + "\tif (largestFile == null || files[i].length > largestFile.length) { \n"
                                + "\t\tlargestFile = files[i]\n"
                                + "\t}\n"
                                + "}\n"
                                + "if (largestFile != null) {\n"
                                + "\tenv.JAVA_ARTIFACT = largestFile.path\n"
                                + "\techo 'Found artifact at ' + largestFile.path\n"
                                + "\techo 'This path is available from the JAVA_ARTIFACT environment variable.'\n"
                                + "}\n"
                        )
                        .build())
                    .build())
                .build())
            .build()))
        .build();
  }
}
