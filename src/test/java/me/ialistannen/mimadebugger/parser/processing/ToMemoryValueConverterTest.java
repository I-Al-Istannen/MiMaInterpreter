package me.ialistannen.mimadebugger.parser.processing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelNode;
import me.ialistannen.mimadebugger.parser.ast.RootNode;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ToMemoryValueConverterTest {

  private ToMemoryValueConverter converter;

  @BeforeEach
  void setup() {
    converter = new ToMemoryValueConverter();
  }

  @Test
  void failOnDanglingLabel() {
    RootNode rootNode = new RootNode(
        Collections.singletonList(
            new LabelNode("test", false, 1, new MutableStringReader(""))
        ),
        new MutableStringReader("")
    );

    assertThrows(
        MiMaSyntaxError.class,
        () -> converter.process(rootNode)
    );
  }

  @Test
  void failOnDanglingInstructionNode() {
    RootNode rootNode = new RootNode(
        Collections.singletonList(
            new InstructionNode("HALT", 1, new MutableStringReader(""))
        ),
        new MutableStringReader("")
    );

    assertThrows(
        MiMaSyntaxError.class,
        () -> converter.process(rootNode)
    );
  }

}