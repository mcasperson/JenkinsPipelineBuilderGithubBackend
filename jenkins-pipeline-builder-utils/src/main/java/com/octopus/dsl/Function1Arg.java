package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a function with a single argument.
 */
@Getter
@SuperBuilder
public class Function1Arg extends ElementWithChildren {

  private String value;

  /**
   * Returns the function with an argument.
   *
   * @return The groovy function
   */
  public String toString() {
    if (value.contains("\n")) {
      return getIndent() + name + " '''\n" + getIndent() + "  " + String.join("\n" + getIndent() + "  ", value.split("\n")) + getIndent() + "\n'''";
    }
    return getIndent() + name + " '" + value + "'";
  }
}
