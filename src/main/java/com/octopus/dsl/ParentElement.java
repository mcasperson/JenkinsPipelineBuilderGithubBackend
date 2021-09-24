package com.octopus.dsl;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class ParentElement extends Element {
    private List<Element> children;

    public String toString() {
        final List<Element> safeChildren = children == null ? List.of() : children;
        safeChildren.forEach(c -> c.parent = this);

        return getIndent() + name + " {\n" +
                safeChildren.stream().map(c -> "\n" + c.toString() + "\n").collect(Collectors.joining()) +
                "\n" + getIndent() + "}";
    }
}
