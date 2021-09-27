package com.octopus.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
public class Argument {
    private String name;
    private String value;
    private ArgType type;

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (!StringUtils.isBlank(name)) {
            builder.append(name);
            builder.append(": ");
        }
        if (type == ArgType.STRING) {
            builder.append("'");
        }
        builder.append(value);
        if (type == ArgType.STRING) {
            builder.append("'");
        }
        return builder.toString();
    }
}
