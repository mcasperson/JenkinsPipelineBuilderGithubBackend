package com.octopus.builders.python;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.builders.java.JavaGitBuilder;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1Arg;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Python builder.
 */
public class PythonBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(PythonBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    return accessor.testFile("requirements.txt");
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return FunctionTrailingLambda.builder()
        .name("pipeline")
        .children(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createTopComments())
            .add(Comment.builder()
                .content(
                    "* JUnit: https://plugins.jenkins.io/junit/")
                .build())
            .add(Function1Arg.builder().name("agent").value("any").build())
            .add(FunctionTrailingLambda.builder()
                .name("stages")
                .children(new ImmutableList.Builder<Element>()
                    .add(GIT_BUILDER.createEnvironmentStage())
                    .add(GIT_BUILDER.createCheckoutStep(accessor))
                    .add(createDependenciesStep(accessor))
                    .add(createTestStep())
                    .add(createPackageStep(accessor))
                    .build())
                .build())
            .build()
        )
        .build()
        .toString();
  }

  private Element createDependenciesStep(@NonNull final RepoClient accessor) {
    final String command = accessor.testFile("setup.py")
        ? "pip install ."
        : "pip install -r requirements.txt";

    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Dependencies")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        command,
                        ArgType.STRING))
                    .build())
                .build())
            .add(Comment.builder()
                .content(
                    "Save the dependencies that went into this build into an artifact. This allows you to review any builds for vulnerabilities later on.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "pip install pipdeptree",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "pipdeptree > dependencies.txt",
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
            .add(Comment.builder()
                .content("List any dependency updates.")
                .build())
            .add(Comment.builder()
                .content(
                    "\"pip list --outdated\" can return the error \"AttributeError: module 'html5lib.treebuilders.etree' has no attribute 'getETreeModule'\"\n"
                    + "in some circumstances. We'll allow this to fail by ensuring the command below always has an exit code of 0, but you can remove the\n"
                    + "\"|| true\" to see any failures.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "pip list --outdated --format=freeze > dependencieupdates.txt || true",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("archiveArtifacts")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("artifacts", "dependencieupdates.txt", ArgType.STRING))
                    .add(new Argument("fingerprint", "true", ArgType.BOOLEAN))
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
                    .add(new Argument(
                        "script",
                        "pip install pytest",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(Comment.builder()
                .content(
                    "Allow pytest to fail by always generating an exit code of zero.\n"
                    + "https://docs.pytest.org/en/latest/reference/exit-codes.html\n"
                    + "The junit plugin can pass or fail depending on the presence or absence of tests.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "pytest --junitxml=results.xml || true",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("junit")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("testResults", "results.xml", ArgType.STRING))
                    .add(new Argument("allowEmptyResults ", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createPackageStep(@NonNull final RepoClient accessor) {
    if (!accessor.testFile("setup.py")) {
      return createZipStep();
    }

    return createSetup();
  }

  private Element createZipStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createGitVersionSteps())
            .add(Function1Arg.builder()
                .name("sh")
                .value("# The Octopus CLI is used to create a package.\n"
                    + "# Get the Octopus CLI from https://octopus.com/downloads/octopuscli#linux\n"
                    + "/usr/bin/octo pack --overwrite --id application --format zip \\\n"
                    + "--include '**/*.py' \\\n"
                    + "--include '**/*.html' \\\n"
                    + "--include '**/*.htm' \\\n"
                    + "--include '**/*.css' \\\n"
                    + "--include '**/*.js' \\\n"
                    + "--include '**/*.min' \\\n"
                    + "--include '**/*.map' \\\n"
                    + "--include '**/*.sql' \\\n"
                    + "--include '**/*.png' \\\n"
                    + "--include '**/*.jpg' \\\n"
                    + "--include '**/*.gif' \\\n"
                    + "--include '**/*.json' \\\n"
                    + "--include '**/*.env' \\\n"
                    + "--include '**/*.txt' \\\n"
                    + "--include '**/Procfile' \\\n"
                    + "--version ${VERSION_SEMVER}")
                .build())
            .build()))
        .build();
  }

  private Element createSetup() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "python setup.py sdist",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }
}
