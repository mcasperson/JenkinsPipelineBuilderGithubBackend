package com.octopus.builders;

import com.google.common.io.Resources;
import com.octopus.builders.java.JavaGradleBuilder;
import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.jenkinsclient.JenkinsClient;
import com.octopus.repoaccessors.GradleTestRepoAccessor;
import com.octopus.repoaccessors.MavenTestRepoAccessor;
import com.octopus.repoaccessors.RepoAccessor;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
public class JavaMavenBuilderTest {

  private static final JavaMavenBuilder JAVA_MAVEN_BUILDER = new JavaMavenBuilder();
  private static final JavaGradleBuilder JAVA_GRADLE_BUILDER = new JavaGradleBuilder();
  private static final PipelineBuilder[] PIPELINE_BUILDERS = {
      JAVA_MAVEN_BUILDER,
      JAVA_GRADLE_BUILDER
  };
  private static final JenkinsClient JENKINS_CLIENT = new JenkinsClient();

  /**
   * A Jenkins container that has the appropriate plugins installed, an admin user setup, the
   * initial wizard disabled, and other customizations.
   */
  @Container
  public GenericContainer jenkins = new GenericContainer(new ImageFromDockerfile()
      .withDockerfileFromBuilder(builder ->
          builder
              .from("jenkins/jenkins:lts")
              .user("root")
              .run("jenkins-plugin-cli --plugins "
                  + "pipeline-utility-steps:2.10.0 "
                  + "gradle:1.37.1 "
                  + "maven-plugin:3.13 "
                  + "jdk-tool:1.5 "
                  + "workflow-aggregator:2.6 "
                  + "git:4.8.2")
              .run("apt-get update")
              .run("apt-get install maven gradle -y")
              .build()))
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/maven_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/maven_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/gradle_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/gradle_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/java_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/java_tool.groovy")
      .withExposedPorts(8080)
      .withEnv("JAVA_OPTS",
          "-Djenkins.install.runSetupWizard=false -Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true");

  @ParameterizedTest
  @MethodSource("provideTestRepos")
  public void verifyTemplate(final String name, final RepoAccessor accessor)
      throws IOException {
    final String template = Arrays.stream(PIPELINE_BUILDERS)
        .filter(p -> p.canBuild(accessor))
        .map(p -> p.generate(accessor))
        .findFirst().get();

    // Add the job to the docker image
    addJobToJenkins(getScriptJob(template), name);

    // print the Jenkins URL
    System.out.println("http://" + jenkins.getHost() + ":" + jenkins.getFirstMappedPort());

    // Now restart jenkins, initiate a build, and check the build result
    final Try<Boolean> success =
        // wait for the server to start
        JENKINS_CLIENT.waitServerStarted(jenkins.getHost(), jenkins.getFirstMappedPort())
            // restart the server to pick up the new jobs
            .flatMap(
                r -> JENKINS_CLIENT.restartJenkins(jenkins.getHost(), jenkins.getFirstMappedPort()))
            // wait for the server to start again
            .flatMap(r -> JENKINS_CLIENT.waitServerStarted(jenkins.getHost(),
                jenkins.getFirstMappedPort()))
            // start building the job
            .flatMap(
                r -> JENKINS_CLIENT.startJob(jenkins.getHost(), jenkins.getFirstMappedPort(), name))
            // wait for the job to finish
            .flatMap(
                r -> JENKINS_CLIENT.waitJobBuilding(jenkins.getHost(), jenkins.getFirstMappedPort(),
                    name))
            // see if the job was a success
            .map(JENKINS_CLIENT::isSuccess);

    // dump the job logs
    JENKINS_CLIENT.getJobLogs(jenkins.getHost(), jenkins.getFirstMappedPort(), name)
        .onSuccess(System.out::println);

    Assertions.assertTrue(success.isSuccess());
    Assertions.assertTrue(success.get());
  }

  private static Stream<Arguments> provideTestRepos() {
    return Stream.of(
        Arguments.of("mavenWrapper",
            new MavenTestRepoAccessor("https://github.com/mcasperson/SampleMavenProject-SpringBoot",
                true)),
        Arguments.of("gradleWrapper", new GradleTestRepoAccessor(
            "https://github.com/mcasperson/SampleGradleProject-SpringBoot", true)),
        Arguments.of("maven",
            new MavenTestRepoAccessor("https://github.com/mcasperson/SampleMavenProject-SpringBoot",
                false)),
        Arguments.of("gradle", new GradleTestRepoAccessor(
            "https://github.com/mcasperson/SampleGradleProject-SpringBoot", false))
    );
  }

  private void addJobToJenkins(final String jobXml, final String jobName) {
    jenkins.copyFileToContainer(
        Transferable.of(jobXml.getBytes(), 0744),
        "/var/jenkins_home/jobs/" + jobName + "/config.xml");

    jenkins.copyFileToContainer(
        Transferable.of(("lastCompletedBuild -1\n"
            + "lastFailedBuild -1\n"
            + "lastStableBuild -1\n"
            + "lastSuccessfulBuild -1\n"
            + "lastUnstableBuild -1\n"
            + "lastUnsuccessfulBuild -1").getBytes(), 0744),
        "/var/jenkins_home/jobs/" + jobName + "/builds/permalinks");

    jenkins.copyFileToContainer(
        Transferable.of("".getBytes(), 0744),
        "/var/jenkins_home/jobs/" + jobName + "/builds/legacyIds");


  }

  private String getScriptJob(final String script) throws IOException {
    final String template = Resources.toString(
        Resources.getResource("jenkins/job_template.xml"),
        StandardCharsets.UTF_8);
    return template.replace("#{Script}", StringEscapeUtils.escapeXml11(script));
  }
}
