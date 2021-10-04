package com.octopus.builders;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Resources;
import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.repoaccessors.RepoAccessor;
import com.octopus.repoaccessors.TestRepoAccessor;
import com.sun.source.tree.TryTree;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.NonNull;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Testcontainers
public class JavaMavenBuilderTest {

  private static final JavaMavenBuilder JAVA_MAVEN_BUILDER = new JavaMavenBuilder();

  /**
   * A Jenkins container that has the appropriate plugins installed, an admin user setup,
   * the initial wizard disabled, and other customizations.
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
      .withEnv("JAVA_OPTS", "-Djenkins.install.runSetupWizard=false -Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true");

  @Test
  public void verifyTemplate() throws IOException {
    final String template = JAVA_MAVEN_BUILDER.generate(
        new TestRepoAccessor("https://github.com/OctopusSamples/RandomQuotes-Java"));

    // Add the job to the docker image
    addJobToJenkins(getScriptJob(template), "maven");

    // Now restart jenkins, initiate a build, and check the build result
    final Try<Boolean> success =
          // wait for the server to start
          waitServerStarted(jenkins.getHost(), jenkins.getFirstMappedPort())
            // restart the server to pick up the new jobs
            .flatMap(r -> restartJenkins(jenkins.getHost(), jenkins.getFirstMappedPort()))
            // wait for the server to start again
            .flatMap(r -> waitServerStarted(jenkins.getHost(), jenkins.getFirstMappedPort()))
            // start building the job
            .flatMap(r -> startJob(jenkins.getHost(), jenkins.getFirstMappedPort(), "maven"))
            // wait for the job to finish
            .flatMap(r -> waitJobBuilding(jenkins.getHost(), jenkins.getFirstMappedPort(), "maven"))
            // see if the job was a success
            .map(this::isSuccess);

    // dump the job logs
    getJobLogs(jenkins.getHost(), jenkins.getFirstMappedPort(), "maven")
        .onSuccess(System.out::println);

    assertTrue(success.isSuccess());
    assertTrue(success.get());
  }

  private Try<String> waitServerStarted(final String hostname, final Integer port) {
    for (int i = 0; i < 12; ++i) {
      final Try<String> serverStarted = getClient()
          .of(httpClient -> postResponse(httpClient, "http://" + hostname + ":" + port + "/login")
              .of(response -> EntityUtils.toString(checkSuccess(response).getEntity())))
          .get();

      if (serverStarted.isSuccess()) {
        return serverStarted;
      }
      Try.run(() -> Thread.sleep(5000));
    }

    return Try.failure(new Exception("Failed to wait for server to start"));
  }

  private CloseableHttpResponse checkSuccess(@NonNull final CloseableHttpResponse response)
      throws Exception {

    final int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code <= 399) {
      return response;
    }

    throw new Exception("Response did not indicate success");
  }

  private Try<Document> waitJobBuilding(final String hostname, final Integer port, final String name) {
    for (int i = 0; i < 240; ++i) {
      final Try<Document> building = getClient()
          .of(httpClient -> postResponse(httpClient,
              "http://" + hostname + ":" + port + "/job/" + name + "/1/api/xml?depth=0")
              .of(response -> EntityUtils.toString(response.getEntity()))
              .mapTry(this::parseXML)
              .get());
      if (building.isSuccess() && !isBuilding(building.get())) {
        return building;
      }
      Try.run(() -> Thread.sleep(5000));
    }

    return Try.failure(new Exception("Failed while waiting for build to complete"));
  }

  private Try<String> getJobLogs(final String hostname, final Integer port, final String name) {
    return getClient()
        .of(httpClient -> postResponse(httpClient,
            "http://" + hostname + ":" + port + "/job/" + name + "/1/consoleText")
            .of(response -> EntityUtils.toString(response.getEntity()))
            .get());
  }

  private boolean isBuilding(final Document doc) {
    return doc.getDocumentElement()
        .getElementsByTagName("building")
        .item(0)
        .getTextContent()
        .equals("true");
  }

  private boolean isSuccess(final Document doc) {
    return doc.getDocumentElement()
        .getElementsByTagName("result")
        .item(0)
        .getTextContent()
        .equals("SUCCESS");
  }

  private Document parseXML(final String xml)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    return dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
  }

  private Try<String> startJob(final String hostname, final Integer port, final String name) {
    return getClient()
        .of(httpClient -> postResponse(httpClient, "http://" + hostname + ":" + port + "/job/" + name + "/build")
            .of(response -> EntityUtils.toString(response.getEntity()))
            .get());
  }

  private Try<String> restartJenkins(final String hostname, final Integer port) {
    return getClient()
        .of(httpClient -> postResponse(httpClient, "http://" + hostname + ":" + port + "/reload")
            .of(response -> EntityUtils.toString(response.getEntity()))
            .get());
  }

  private Try.WithResources1<CloseableHttpClient> getClient() {
    return Try.withResources(HttpClients::createDefault);
  }

  private Try.WithResources1<CloseableHttpResponse> postResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path) {
    return Try.withResources(() -> httpClient.execute(new HttpPost(path)));
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
