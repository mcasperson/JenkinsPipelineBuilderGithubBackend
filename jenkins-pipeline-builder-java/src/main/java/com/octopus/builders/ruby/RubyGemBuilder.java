package com.octopus.builders.ruby;

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
import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;
import org.jboss.logging.Logger;

public class RubyGemBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(RubyGemBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    return accessor.testFile("Gemfile");
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
                    .add(createDependenciesStep())
                    .add(createTestStep())
                    .add(createPackageStep(accessor))
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
                        "bundle install",
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
                        "bundle exec rspec --format RspecJunitFormatter --out results.xml",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("junit")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("", "results.xml", ArgType.STRING))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createPackageStep(@NonNull final RepoClient accessor) {
    final Try<List<String>> gemSpecs = accessor.getWildcardFiles("*.gemspec");

    if (gemSpecs.isFailure() || gemSpecs.get().isEmpty()) {
      return createZipStep();
    }

    return createSetup(gemSpecs.get().get(0));
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
                    + "--include **/*.rb \\\n"
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
                    + "--version 1.0.0.${BUILD_NUMBER}")
                .build())
            .build()))
        .build();
  }

  private Element createSetup(@NonNull final String gemspec) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "gem build " + gemspec,
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }
}
