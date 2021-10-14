package com.octopus.builders;

import com.octopus.builders.dotnet.DotnetCoreBuilder;
import com.octopus.builders.nodejs.NodejsBuilder;
import com.octopus.builders.php.PhpComposerBuilder;
import com.octopus.repoclients.DotnetTestRepoClient;
import com.octopus.repoclients.NodeTestRepoClient;
import com.octopus.repoclients.PhpTestRepoClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * These tests are just a convenient way to print the templates for manual testing in a Jenkins
 * instance.
 */
public class PrintTemplates {

  @Test
  public void printDotNetTemplate() {
    final String template =
        new DotnetCoreBuilder()
            .generate(new DotnetTestRepoClient("https://github.com/OctopusSamples/RandomQuotes"));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

  @Test
  public void printNodejsTemplate() {
    final String template =
        new NodejsBuilder()
            .generate(new NodeTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-js"));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

  @Test
  public void printPhpTemplate() {
    final String template =
        new PhpComposerBuilder()
            .generate(new PhpTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-PHP"));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

}
