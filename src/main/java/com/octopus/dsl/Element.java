package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class Element {
    protected Element parent;
    protected String name;
    private List<Element> children;

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

    protected List<Element> getSafeChildren() {
        return children == null ? List.of() : children;
    }
}
