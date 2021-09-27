package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class ElementWithChildren extends Element {
    protected String name;
    private List<Element> children;

    protected List<Element> getSafeChildren() {
        return children == null ? List.of() : children;
    }
}
