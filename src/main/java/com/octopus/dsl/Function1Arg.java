package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Function1Arg extends Element {
    private String value;

    public String toString() {
        return getIndent() + name + " '" + value + "'";
    }
}
