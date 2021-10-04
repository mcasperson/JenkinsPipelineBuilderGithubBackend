package com.octopus.builders;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
public class JavaMavenBuilderTest {

  /**
   * A Jenkins container that has the appropriate plugins installed, an admin user setup,
   * the initial wizard disabled, and other customizations.
   */
  @Container
  public GenericContainer jenkins = new GenericContainer(new ImageFromDockerfile()
      .withDockerfileFromBuilder(builder ->
          builder
              .from("jenkins/jenkins:lts")
              .run("jenkins-plugin-cli --plugins pipeline-utility-steps:2.10.0 gradle:1.37.1 maven-plugin:3.13 jdk-tool:1.5 workflow-aggregator:2.6")
              .user("root")
              .run("apt-get update")
              .run("apt-get install maven gradle -y")
              .user("jenkins")
              .build()))
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/maven_tool.groovy"), "/usr/share/jenkins/ref/init.groovy.d/maven_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/gradle_tool.groovy"), "/usr/share/jenkins/ref/init.groovy.d/gradle_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/java_tool.groovy"), "/usr/share/jenkins/ref/init.groovy.d/java_tool.groovy")
      //.withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/config.xml"), "/usr/share/jenkins/ref/users/config.xml")
      //.withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/jenkins_home_config.xml"), "/usr/share/jenkins/ref/config.xml")
      .withExposedPorts(8080)
      .withEnv("JAVA_OPTS", "-Djenkins.install.runSetupWizard=false -Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true");

  @Test
  public void verifyTemplate() {
    final String address = jenkins.getHost();
    final Integer port = jenkins.getFirstMappedPort();

    System.out.print("blah");
  }
}
