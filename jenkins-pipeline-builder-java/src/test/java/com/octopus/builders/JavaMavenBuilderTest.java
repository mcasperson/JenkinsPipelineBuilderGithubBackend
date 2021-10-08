package com.octopus.builders;

import com.google.common.io.Resources;
import com.octopus.builders.go.GoBuilder;
import com.octopus.builders.java.JavaGradleBuilder;
import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.builders.nodejs.NodejsNpmBuilder;
import com.octopus.builders.php.PhpComposerBuilder;
import com.octopus.builders.python.PythonBuilder;
import com.octopus.builders.ruby.RubyGemBuilder;
import com.octopus.jenkinsclient.JenkinsClient;
import com.octopus.jenkinsclient.JenkinsDetails;
import com.octopus.repoclients.GoTestRepoClient;
import com.octopus.repoclients.GradleTestRepoClient;
import com.octopus.repoclients.MavenTestRepoClient;
import com.octopus.repoclients.NodeTestRepoClient;
import com.octopus.repoclients.PhpTestRepoClient;
import com.octopus.repoclients.PythonTestRepoClient;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RubyTestRepoClient;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.NonNull;
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
  private static final PythonBuilder PYTHON_BUILDER  = new PythonBuilder();
  private static final PhpComposerBuilder PHP_COMPOSER_BUILDER  = new PhpComposerBuilder();
  private static final NodejsNpmBuilder NODEJS_NPM_BUILDER  = new NodejsNpmBuilder();
  private static final RubyGemBuilder RUBY_GEM_BUILDER  = new RubyGemBuilder();
  private static final GoBuilder GO_BUILDER  = new GoBuilder();
  private static final PipelineBuilder[] PIPELINE_BUILDERS = {
      JAVA_MAVEN_BUILDER,
      JAVA_GRADLE_BUILDER,
      PYTHON_BUILDER,
      PHP_COMPOSER_BUILDER,
      NODEJS_NPM_BUILDER,
      RUBY_GEM_BUILDER,
      GO_BUILDER
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
              // let the jenkins user run sudo
              .run("echo \"jenkins ALL=(ALL) NOPASSWD: ALL\" >> /etc/sudoers")
              // install plugins
              .run("jenkins-plugin-cli --plugins "
                  + "pipeline-utility-steps:2.10.0 "
                  + "gradle:1.37.1 "
                  + "maven-plugin:3.13 "
                  + "jdk-tool:1.5 "
                  + "workflow-aggregator:2.6 "
                  + "git:4.8.2 "
                  + "msbuild:1.30 "
                  + "mstest:1.0.0")
              .run("apt-get update")
              // Install php, ruby, python
              .run("apt-get install vim maven wget curl sudo python3 python3-pip python3-html5lib ruby-full ruby-dev php7.4 php-cli php-zip php-dom php-mbstring unzip -y")
              // install gradle
              .run("wget https://services.gradle.org/distributions/gradle-7.2-bin.zip")
              .run("unzip gradle-7.2-bin.zip")
              .run("mv gradle-7.2 /opt")
              .run("chmod +x /opt/gradle-7.2/bin/gradle")
              .run("ln -s /opt/gradle-7.2/bin/gradle /usr/bin/gradle")
              // install jdk 17
              .run("wget https://cdn.azul.com/zulu/bin/zulu17.28.13-ca-jdk17.0.0-linux_x64.tar.gz")
              .run("tar -xzf zulu17.28.13-ca-jdk17.0.0-linux_x64.tar.gz")
              .run("mv zulu17.28.13-ca-jdk17.0.0-linux_x64 /opt")
              // install dotnet
              .run("wget https://packages.microsoft.com/config/debian/11/packages-microsoft-prod.deb -O packages-microsoft-prod.deb")
              .run("dpkg -i packages-microsoft-prod.deb")
              .run("apt-get update; apt-get install -y apt-transport-https && apt-get update && apt-get install -y dotnet-sdk-5.0 dotnet-sdk-3.1")
              // install octocli
              .run("apt update && sudo apt install -y --no-install-recommends gnupg curl ca-certificates apt-transport-https && "
                  + "curl -sSfL https://apt.octopus.com/public.key | apt-key add - && "
                  + "sh -c \"echo deb https://apt.octopus.com/ stable main > /etc/apt/sources.list.d/octopus.com.list\" && "
                  + "apt update && sudo apt install -y octopuscli")
              // install nodejs
              .run("curl -fsSL https://deb.nodesource.com/setup_16.x | bash -")
              .run("apt-get install -y nodejs")
              // install yarn
              .run("curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | sudo apt-key add -")
              .run("echo \"deb https://dl.yarnpkg.com/debian/ stable main\" | sudo tee /etc/apt/sources.list.d/yarn.list")
              .run("sudo apt update; sudo apt install yarn")
              // install composer
              .run("wget -O composer-setup.php https://getcomposer.org/installer")
              .run("sudo php composer-setup.php --install-dir=/usr/local/bin --filename=composer")
              // install golang
              .run("wget https://golang.org/dl/go1.17.1.linux-amd64.tar.gz")
              .run("rm -rf /usr/local/go && tar -C /usr/local -xzf go1.17.1.linux-amd64.tar.gz")
              .env("PATH", "/usr/local/go/bin:/root/go/bin:${PATH}")
              .build()))
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/maven_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/maven_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/gradle_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/gradle_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/java_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/java_tool.groovy")
      .withExposedPorts(8080)
      .withEnv("JAVA_OPTS",
          "-Djenkins.install.runSetupWizard=false "
              + "-Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true");

  @ParameterizedTest
  @MethodSource("provideTestRepos")
  public void verifyTemplate(@NonNull final String name, @NonNull final RepoClient accessor)
      throws IOException {
    final String template = Arrays.stream(PIPELINE_BUILDERS)
        .filter(p -> p.canBuild(accessor))
        .map(p -> p.generate(accessor))
        .findFirst().get();

    // Add the job to the docker image
    addJobToJenkins(getScriptJob(template), name);

    final JenkinsDetails jenkinsDetails = new JenkinsDetails(
        jenkins.getHost(),
        jenkins.getFirstMappedPort());

    // print the Jenkins URL
    System.out.println("Jenkins URL: " + jenkinsDetails);

    // Now restart jenkins, initiate a build, and check the build result
    final Try<Boolean> success =
        // wait for the server to start
        JENKINS_CLIENT.waitServerStarted(jenkinsDetails)
            // restart the server to pick up the new jobs
            .flatMap(r -> JENKINS_CLIENT.restartJenkins(jenkinsDetails))
            // wait for the server to start again
            .flatMap(r -> JENKINS_CLIENT.waitServerStarted(jenkinsDetails))
            // start building the job
            .flatMap(r -> JENKINS_CLIENT.startJob(jenkinsDetails, name))
            // wait for the job to finish
            .flatMap(r -> JENKINS_CLIENT.waitJobBuilding(jenkinsDetails, name))
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
        Arguments.of(
            "go",
            new GoTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-Go",
                "main")),
        Arguments.of(
            "ruby",
            new RubyTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-Ruby",
                "master")),
        Arguments.of(
            "php",
            new NodeTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-JS")),
        Arguments.of(
            "php",
            new PhpTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-PHP")),
        Arguments.of(
            "python",
            new PythonTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-Python",
                "main")),
        Arguments.of(
            "gradle",
            new GradleTestRepoClient(
                "https://github.com/mcasperson/SampleGradleProject-SpringBoot",
                false)),
        Arguments.of(
            "maven",
            new MavenTestRepoClient(
                "https://github.com/mcasperson/SampleMavenProject-SpringBoot",
                false)),
        Arguments.of(
            "mavenWrapper",
            new MavenTestRepoClient(
                "https://github.com/mcasperson/SampleMavenProject-SpringBoot",
                true)),
        Arguments.of(
            "mavenWrapperQuarkus",
            new MavenTestRepoClient(
                "https://github.com/mcasperson/SampleMavenProject-Quarkus",
                true)),
        Arguments.of(
            "gradleWrapper",
            new GradleTestRepoClient(
                "https://github.com/mcasperson/SampleGradleProject-SpringBoot",
                true))
    );
  }

  private void addJobToJenkins(@NonNull final String jobXml, @NonNull final String jobName) {
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

  private String getScriptJob(@NonNull final String script) throws IOException {
    final String template = Resources.toString(
        Resources.getResource("jenkins/job_template.xml"),
        StandardCharsets.UTF_8);
    return template.replace("#{Script}", StringEscapeUtils.escapeXml11(script));
  }
}
