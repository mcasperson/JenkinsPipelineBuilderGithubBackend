package com.octopus.builders;

import com.octopus.builders.dotnet.DotnetCoreBuilder;
import com.octopus.repoclients.DotnetTestRepoClient;
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
      Assertions.assertNotEquals( "", template );
      System.out.println(template);
  }

}
