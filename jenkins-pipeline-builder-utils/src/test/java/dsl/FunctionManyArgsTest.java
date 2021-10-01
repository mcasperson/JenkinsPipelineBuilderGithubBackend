package dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import org.junit.jupiter.api.Test;

public class FunctionManyArgsTest {
  @Test
  public void testFunction1Arg() {
    final Element element = FunctionManyArgs.builder()
        .name("function")
        .args(new ImmutableList.Builder<Argument>()
            .add(new Argument("name", "value", ArgType.STRING))
            .build())
        .build();
    assertEquals("function(name: 'value')", element.toString());
  }
}
