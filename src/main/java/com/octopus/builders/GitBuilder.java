package com.octopus.builders;

import com.google.common.collect.ImmutableList;
import com.octopus.dsl.*;
import com.octopus.repoaccessors.RepoAccessor;
import lombok.NonNull;

import java.util.List;

public class GitBuilder {
    public boolean fileExists(@NonNull final RepoAccessor accessor, @NonNull final String file) {
        return accessor.testFile("blob/" + accessor.getDefaultBranch() + "/" + file);
    }

    public Element createCheckoutStep(@NonNull final RepoAccessor accessor) {
        return Function1ArgTrailingLambda.builder()
                .name("stage")
                .arg("Checkout")
                .children(createStepsElement(new ImmutableList.Builder<Element>()
                        .add(FunctionManyArgs.builder()
                                .name("checkout")
                                .args(new ImmutableList.Builder<Argument>()
                                        .add(new Argument("$class", "GitSCM", ArgType.STRING))
                                        .add(new Argument("userRemoteConfigs", "[[url: '" + accessor.getRepoPath() + "']]", ArgType.ARRAY))
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    public List<Element> createStepsElement(List<Element> children) {
        return new ImmutableList.Builder<Element>().add(
                        FunctionTrailingLambda.builder()
                                .name("steps")
                                .children(children)
                                .build())
                .build();

    }

    public Element createDeployStep(@NonNull final String buildDir) {
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
                                                        "def files = (findFiles(glob: '" + buildDir + "/*.jar') + findFiles(glob: '" + buildDir + "/**.war')).sort{x, y ->\n" +
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
}
