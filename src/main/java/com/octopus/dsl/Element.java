package com.octopus.dsl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Element {
    protected Element parent;
    protected String name;

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
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < depth; ++i) {
            builder.append("  ");
        }
        return builder.toString();
    }
}
