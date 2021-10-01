package dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1Arg;
import org.junit.jupiter.api.Test;

public class Function1ArgTest {

  @Test
  public void testFunction1Arg() {
    final Element element = Function1Arg.builder()
        .name("function")
        .value("argument")
        .build();
    assertEquals("function 'argument'", element.toString());
  }
}
