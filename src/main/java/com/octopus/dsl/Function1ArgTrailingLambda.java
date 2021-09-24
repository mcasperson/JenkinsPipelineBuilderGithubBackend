package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class Function1ArgTrailingLambda extends FunctionTrailingLambda {
    private String arg;

    public String toString() {
        final List<Element> safeChildren = getSafeChildren();
        safeChildren.forEach(c -> c.parent = this);

        return getIndent() + name + "('" + arg + "') {\n" +
                safeChildren.stream().map(Object::toString).collect(Collectors.joining("\n")) +
                "\n" + getIndent() + "}";
    }
}
