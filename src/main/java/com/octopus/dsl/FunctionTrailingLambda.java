package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class FunctionTrailingLambda extends Element {
    private List<Element> children;

    public String toString() {
        final List<Element> safeChildren = getSafeChildren();
        safeChildren.forEach(c -> c.parent = this);

        return getIndent() + name + " {\n" +
                safeChildren.stream().map(Object::toString).collect(Collectors.joining("\n")) +
                "\n" + getIndent() + "}";
    }

    protected List<Element> getSafeChildren() {
        return children == null ? List.of() : children;
    }
}
