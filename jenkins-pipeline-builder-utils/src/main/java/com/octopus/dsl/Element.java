package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * The base class for all the DSL elements.
 */
@Getter
@SuperBuilder
public class Element {

  protected Element parent;

  protected int getDepth() {
    Element currentParent = parent;
    int depth = 0;
    while (currentParent != null) {
      ++depth;
      currentParent = currentParent.parent;
    }
    return depth;
  }

  protected String getIndent() {
    final int depth = getDepth();
    return "  ".repeat(Math.max(0, depth));
  }


}