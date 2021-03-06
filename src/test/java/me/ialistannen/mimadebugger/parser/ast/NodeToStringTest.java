package me.ialistannen.mimadebugger.parser.ast;

import java.util.Collections;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Load;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;
import org.junit.jupiter.api.Test;

public class NodeToStringTest {

  @Test
  void constantToStringCompletesNormally() {
    new ConstantNode(1, 20, new MutableStringReader(""), HalfOpenIntRange.ZERO);
  }

  @Test
  void labelToStringCompletesNormally() {
    new LabelDeclarationNode("", 1, new MutableStringReader(""), HalfOpenIntRange.ZERO);
  }

  @Test
  void instructionToStringCompletesNormally() {
    new InstructionNode(" ", 1, new MutableStringReader(""), HalfOpenIntRange.ZERO);
  }

  @Test
  void instructionCallToStringCompletesNormally() {
    ImmutableInstructionCall instructionCall = ImmutableInstructionCall.builder()
        .command(Load.LOAD_CONSTANT)
        .argument(1)
        .build();
    new InstructionCallNode(
        1,
        new MutableStringReader(""),
        instructionCall,
        HalfOpenIntRange.ZERO
    );
  }

  @Test
  void rootNodeToStringCompletesNormally() {
    new RootNode(Collections.emptyList(), new MutableStringReader(""));
  }


}
