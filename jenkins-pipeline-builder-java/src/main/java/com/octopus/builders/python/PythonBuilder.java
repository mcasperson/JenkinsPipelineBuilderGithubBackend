package com.octopus.builders.python;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.builders.java.JavaGitBuilder;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1Arg;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.jboss.logging.Logger;

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
        .arg("Install Dependencies")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        command,
                        ArgType.STRING))
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
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "pytest --junitxml=results.xml",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("junit")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("", "results.xml", ArgType.STRING))
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
            .add(Function1Arg.builder()
                .name("sh")
                .value("# The Octopus CLI is used to create a package.\n"
                    + "# Get the Octopus CLI from https://octopus.com/downloads/octopuscli#linux\n"
                    + "/usr/bin/octo pack --id application --format zip \\\n"
                    + "--include **/*.py \\\n"
                    + "--include **/*.html \\\n"
                    + "--include **/*.htm \\\n"
                    + "--include **/*.css \\\n"
                    + "--include **/*.js \\\n"
                    + "--include **/*.min \\\n"
                    + "--include **/*.map \\\n"
                    + "--include **/*.sql \\\n"
                    + "--include **/*.png \\\n"
                    + "--include **/*.jpg \\\n"
                    + "--include **/*.gif \\\n"
                    + "--include **/*.json \\\n"
                    + "--include **/*.env \\\n"
                    + "--include **/*.txt \\\n"
                    + "--include **/Procfile \\\n"
                    + "--version 1.0.0.${BUILD_NUMBER}")
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
