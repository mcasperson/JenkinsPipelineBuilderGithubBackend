package com.octopus.builders.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.*;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.NonNull;

import java.util.List;

public class JavaBuilder implements PipelineBuilder {

    private final RepoAccessor accessor;

    public JavaBuilder(@NonNull final RepoAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public Boolean canBuild() {
        return accessor.getFile("pom.xml").isSuccess() ||
                accessor.getFile("build.gradle").isSuccess() ||
                accessor.getFile("build.gradle.kts").isSuccess();
    }

    @Override
    public String generate() {
        return FunctionTrailingLambda.builder()
                .name("pipeline")
                .children(new ImmutableList.Builder<Element>()
                        .add(FunctionTrailingLambda.builder()
                                .name("tools")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(Function1Arg.builder().name("maven").value("Maven").build())
                                        .add(Function1Arg.builder().name("jdk").value("Java").build())
                                        .build()
                                )
                                .build())
                        .add(Function1Arg.builder().name("agent").value("any").build())
                        .add(FunctionTrailingLambda.builder()
                                .name("stages")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(createDependenciesStep())
                                        .add(createBuildStep())
                                        .add(createTestStep())
                                        .add(createPackageStep())
                                        .add(createDeployStep())
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
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode dependency:tree > dependencies.txt", ArgType.STRING))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("archiveArtifacts")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("artifacts", "dependencies.txt", ArgType.STRING))
                                        .add(new Argument("fingerprint", "true", ArgType.BOOLEAN))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode versions:display-dependency-updates > dependencieupdates.txt", ArgType.STRING))
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

    private Element createBuildStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Build")
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode versions:set -DnewVersion=1.0.${BUILD_NUMBER}", ArgType.STRING))
                                        .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode compile", ArgType.STRING))
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
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode test", ArgType.STRING))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("junit")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("", "build/reports/**/*.xml", ArgType.STRING))
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    private Element createPackageStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Package")
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", "mvn --batch-mode package -DskipTests", ArgType.STRING))
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    private Element createDeployStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Deploy")
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionTrailingLambda.builder()
                                .name("script")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(StringContent.builder()
                                                .content("new File(\"${workspace}/target\").eachFileRecurse (FileType.FILES)\n" +
                                                        "    {file ->\n" +
                                                        "        if (file.getName().endsWith('.jar') || file.getName().endsWith('.war')) {\n" +
                                                        "            env.JAVA_ARTIFACT = file.getAbsolutePath()\n" +
                                                        "            echo 'Found artifact at' + file.getAbsolutePath()\n" +
                                                        "            echo 'This path is available from the JAVA_ARTIFACT environment variable.'\n" +
                                                        "        }\n" +
                                                        "    }")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    private List<Element> createStepsElement(List<Element> children) {
        return new ImmutableList.Builder<Element>().add(
                        Function1ArgTrailingLambda.builder()
                                .name("stage")
                                .arg("Deploy")
                                .children(children)
                                .build())
                .build();

    }
}
