package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Comment extends Element {
    private String content;

    public String toString() {
        return getIndent() + "# " + content;
    }
}
