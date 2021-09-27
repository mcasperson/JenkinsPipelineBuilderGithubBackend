package com.octopus.dsl;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class StringContent extends Element {

  private String content;

  public String toString() {
    return getIndent() + "  " + Arrays.stream(content.split("\n"))
        .collect(Collectors.joining("\n" + getIndent() + "  "));
  }
}
