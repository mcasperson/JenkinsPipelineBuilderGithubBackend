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

    public List<Element> createTopComments() {
        return new ImmutableList.Builder<Element>()
            .add(Comment.builder()
                .content("This pipeline requires the Pipeline Utility Steps Plugin: https://wiki.jenkins.io/display/JENKINS/Pipeline+Utility+Steps+Plugin")
                .build())
            .build();
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
                                .content("This scans through the build tool output directory and find the largest file, which we assume is the artifact that was intended to be deployed.\n" +
                                        "The path to this file is saved in and environment variable called JAVA_ARTIFACT, which can be consumed by subsequent custom deployment steps.")
                                .build())
                        .add(FunctionTrailingLambda.builder()
                                .name("script")
                                .children(new ImmutableList.Builder<Element>()
                                        .add(StringContent.builder()
                                                .content(
                                                        "// Find the matching artifacts\n" +
                                                        "def extensions = ['jar', 'war']\n" +
                                                        "def files = []\n" +
                                                        "for(extension in extensions){\n" +
                                                        "    findFiles(glob: '" + buildDir + "/**.' + extension).each{files << it}\n" +
                                                        "}\n"+
                                                        "echo 'Found ' + files.size() + ' potential artifacts'\n" +
                                                        "// Assume the largest JAR or WAR is the artifact we intend to deploy\n" +
                                                        "def largestFile = null\n" +
                                                        "for (i = 0; i < files.size(); ++i) {\n" +
                                                        "\tif (largestFile == null || files[i].length > largestFile.length) { \n"+
                                                        "\t\tlargestFile = files[i]\n" +
                                                        "\t}\n" +
                                                        "}\n" +
                                                        "if (largestFile != null) {\n" +
                                                        "\tenv.JAVA_ARTIFACT = largestFile.path\n" +
                                                        "\techo 'Found artifact at ' + largestFile.path\n" +
                                                        "\techo 'This path is available from the JAVA_ARTIFACT environment variable.'\n" +
                                                        "}\n"
                                                        )
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .build();
    }
}
