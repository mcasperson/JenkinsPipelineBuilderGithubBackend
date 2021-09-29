package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a function with many arguments.
 */
@Getter
@SuperBuilder
public class FunctionManyArgs extends ElementWithChildren {

    private List<Argument> args;

    /**
     * Builds a function with many arguments.
     *
     * @return The groovy function.
     */
    public String toString() {
        final List<Element> safeChildren = getSafeChildren();
        safeChildren.forEach(c -> c.parent = this);

        return getIndent() + name + "(" + args.stream().map(Argument::toString)
                .collect(Collectors.joining(", ")) + ")";
    }
}
