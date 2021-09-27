package com.octopus.builders.java;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.*;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.NonNull;

import java.util.List;

public class JavaMavenBuilder implements PipelineBuilder {

    private boolean usesWrapper = false;

    @Override
    public Boolean canBuild(@NonNull final RepoAccessor accessor) {
        if (accessor.getFile("pom.xml").isSuccess()) {
            usesWrapper = usesWrapper(accessor);
            return true;
        }

        return false;
    }

    @Override
    public String generate(@NonNull final RepoAccessor accessor) {
        return FunctionTrailingLambda.builder()
                .name("pipeline")
                .children(new ImmutableList.Builder<Element>()
                        .add(FunctionTrailingLambda.builder()
                                .name("tools")
                                .children(createTools())
                                .build())
                        .add(Function1Arg.builder().name("agent").value("any").build())
                        .add(FunctionTrailingLambda.builder()
                                .name("stages")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(createCheckoutStep(accessor))
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

    private Boolean usesWrapper(@NonNull final RepoAccessor accessor) {
        return accessor.getFile("mvnw").isSuccess();
    }

    private String mavenExecutable() {
        return usesWrapper ? "./mvnw" : "mvn";
    }

    private List<Element> createTools() {
        final ImmutableList.Builder<Element> list = new ImmutableList.Builder<Element>()
                .add(Function1Arg.builder().name("jdk").value("Java").build());

        if (!usesWrapper) {
            list.add(Function1Arg.builder().name("maven").value("Maven").build());
        }

        return list.build();
    }

    private Element createDependenciesStep() {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("List Dependencies")
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(Comment.builder()
                                .content("Download the dependencies and plugins before we attempt to do any further actions")
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", mavenExecutable() + " --batch-mode dependency:resolve-plugins dependency:go-offline", ArgType.STRING))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", mavenExecutable() + " --batch-mode dependency:tree > dependencies.txt", ArgType.STRING))
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
                                        .add(new Argument("script", mavenExecutable() + " --batch-mode versions:display-dependency-updates > dependencieupdates.txt", ArgType.STRING))
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

    private Element createCheckoutStep(@NonNull final RepoAccessor accessor) {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Checkout")
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("checkout")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("$class", "GitSCM", ArgType.STRING))
                                        .add(new Argument("branches", "[[name: '*/master']]", ArgType.ARRAY))
                                        .add(new Argument("userRemoteConfigs", "[[url: '" + accessor.getRepoPath() + "']]", ArgType.ARRAY))
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
                                        .add(new Argument("script", mavenExecutable() + " --batch-mode versions:set -DnewVersion=1.0.${BUILD_NUMBER}", ArgType.STRING))
                                        .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("sh")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("script", mavenExecutable() + " --batch-mode compile", ArgType.STRING))
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
                                        .add(new Argument("script", mavenExecutable() + " --batch-mode test", ArgType.STRING))
                                        .build())
                                .build())
                        .add(FunctionManyArgs.builder()
                                .name("junit")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("", "target/surefire-reports/*.xml", ArgType.STRING))
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
                                        .add(new Argument("script", mavenExecutable() + " --batch-mode package -DskipTests", ArgType.STRING))
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
                        .add(Comment.builder()
                                .content("This requires the Pipeline Utility Steps Plugin: https://wiki.jenkins.io/display/JENKINS/Pipeline+Utility+Steps+Plugin")
                                .build())
                        .add(FunctionTrailingLambda.builder()
                                .name("script")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(StringContent.builder()
                                                .content("// Assume the largest JAR or WAR is the artifact we intended to build\n" +
                                                        "def files = (findFiles(glob: 'target/*.jar') + findFiles(glob: 'target/*.war')).sort{x, y ->\n" +
                                                        "\treturn x.length > y.length ? -1 : 1\n" +
                                                        "}\n" +
                                                        "if (files.size() != 0) {\n" +
                                                        "\tenv.JAVA_ARTIFACT = files[0].path\n" +
                                                        "\techo 'Found artifact at ' + files[0].path\n" +
                                                        "\techo 'This path is available from the JAVA_ARTIFACT environment variable.'\n" +
                                                        "}")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    private List<Element> createStepsElement(List<Element> children) {
        return new ImmutableList.Builder<Element>().add(
                        FunctionTrailingLambda.builder()
                                .name("steps")
                                .children(children)
                                .build())
                .build();

    }
}
