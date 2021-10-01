package dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1Arg;
import com.octopus.dsl.Function1ArgTrailingLambda;
import org.junit.jupiter.api.Test;

public class Function1ArgTrailingLambdaTest {
  @Test
  public void testFunction1Arg() {
    final Element element = Function1ArgTrailingLambda.builder()
        .name("function")
        .arg("argument")
        .children(new ImmutableList.Builder<Element>()
            .add(Comment.builder().content("comment").build())
            .build())
        .build();
    assertEquals("function('argument') {\n  // comment\n}", element.toString());
  }
}
