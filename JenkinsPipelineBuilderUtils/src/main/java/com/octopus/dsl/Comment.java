package com.octopus.dsl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class Comment extends Element {
    private String content;

    public String toString() {
        return Arrays.stream(content.split("\n")).map(c -> getIndent() + "// " + c).collect(Collectors.joining("\n"));
    }
}
