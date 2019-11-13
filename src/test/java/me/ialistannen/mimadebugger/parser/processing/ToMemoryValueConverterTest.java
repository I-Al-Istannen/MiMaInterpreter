package me.ialistannen.mimadebugger.parser.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Optional;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelUsageNode;
import me.ialistannen.mimadebugger.parser.ast.RootNode;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;
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
            new LabelUsageNode("test", 1, new MutableStringReader(""), HalfOpenIntRange.ZERO)
        ),
        new MutableStringReader("")
    );

    assertEquals(
        Optional.empty(),
        converter.process(rootNode)
    );
  }

  @Test
  void failOnDanglingInstructionNode() {
    RootNode rootNode = new RootNode(
        Collections.singletonList(
            new InstructionNode("HALT", 1, new MutableStringReader(""), HalfOpenIntRange.ZERO)
        ),
        new MutableStringReader("")
    );

    assertEquals(
        Optional.empty(),
        converter.process(rootNode)
    );
  }

}