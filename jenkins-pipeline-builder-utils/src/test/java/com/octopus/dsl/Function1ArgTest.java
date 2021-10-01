package com.octopus.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Function1ArgTest {

  @Test
  public void testFunction1Arg() {
    final Element element = Function1Arg.builder()
        .name("function")
        .value("argument")
        .build();
    assertEquals("function 'argument'", element.toString());
  }
}
