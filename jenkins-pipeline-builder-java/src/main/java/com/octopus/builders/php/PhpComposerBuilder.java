package com.octopus.builders.php;

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

public class PhpComposerBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(PhpComposerBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    return accessor.testFile("composer.json");
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
                    .add(createTestStep())
                    .add(createPackageStep())
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
        .arg("Install Dependencies")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "composer install",
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
                        "vendor/phpunit/phpunit/phpunit --bootstrap build/bootstrap.php",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createPackageStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(Function1Arg.builder()
                .name("sh")
                .value("# The Octopus CLI is used to create a package.\n"
                    + "# Get the Octopus CLI from https://octopus.com/downloads/octopuscli#linux\n"
                    + "/usr/bin/octo pack --id application --format zip "
                    + "--include **/*.php "
                    + "--include **/*.html "
                    + "--include **/*.htm "
                    + "--include **/*.css "
                    + "--include **/*.js "
                    + "--include **/*.sql "
                    + "--include **/*.png "
                    + "--include **/*.jpg "
                    + "--include **/*.gif "
                    + "--include **/*.json "
                    + "--include **/*.env "
                    + "--version 1.0.0.${BUILD_NUMBER}")
                .build())
            .build()))
        .build();
  }
}
