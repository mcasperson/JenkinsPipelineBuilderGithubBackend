package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class FunctionManyArgs extends ElementWithChildren {
    private List<Argument> args;

    public String toString() {
        final List<Element> safeChildren = getSafeChildren();
        safeChildren.forEach(c -> c.parent = this);

        return getIndent() + name + "(" + args.stream().map(Argument::toString).collect(Collectors.joining(", ")) + ")";
    }
}
