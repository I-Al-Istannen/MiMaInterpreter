package me.ialistannen.mimadebugger.parser.ast;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicBoolean;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Load;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;
import org.junit.jupiter.api.Test;

class NodeVisitorTest {

  @Test
  void constantDispatchMethodCalled() throws MiMaSyntaxError {
    AtomicBoolean hit = new AtomicBoolean();
    new NodeVisitor() {
      @Override
      public void visitConstantNode(ConstantNode constantNode) {
        hit.set(true);
      }
    }.visit(new ConstantNode(1, 1, new MutableStringReader(""), HalfOpenIntRange.ZERO));

    assertThat(
        hit.get(),
        is(true)
    );
  }

  @Test
  void labelDispatchMethodCalled() throws MiMaSyntaxError {
    AtomicBoolean hit = new AtomicBoolean();
    new NodeVisitor() {
      @Override
      public void visitLabelNode(LabelNode labelNode) {
        hit.set(true);
      }
    }.visit(new LabelNode("", true, 1, new MutableStringReader(""), HalfOpenIntRange.ZERO));

    assertThat(
        hit.get(),
        is(true)
    );
  }

  @Test
  void instructionNodeDispatchMethodCalled() throws MiMaSyntaxError {
    AtomicBoolean hit = new AtomicBoolean();
    new NodeVisitor() {

      @Override
      public void visitInstructionNode(InstructionNode instructionNode) {
        hit.set(true);
      }
    }.visit(new InstructionNode("", 1, new MutableStringReader(""), HalfOpenIntRange.ZERO));

    assertThat(
        hit.get(),
        is(true)
    );
  }

  @Test
  void instructionCallNodeDispatchMethodCalled() throws MiMaSyntaxError {
    AtomicBoolean hit = new AtomicBoolean();
    new NodeVisitor() {

      @Override
      public void visitInstructionCallNode(InstructionCallNode instructionCallNode) {
        hit.set(true);
      }
    }.visit(
        new InstructionCallNode(
            1,
            new MutableStringReader(""),
            ImmutableInstructionCall.builder()
                .argument(1)
                .command(Load.LOAD_CONSTANT)
                .build(),
            HalfOpenIntRange.ZERO
        )
    );

    assertThat(
        hit.get(),
        is(true)
    );
  }

  @Test
  void invalidTypeThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new NodeVisitor() {
        }.visit(new AbstractSyntaxTreeNode(1, new MutableStringReader(""), HalfOpenIntRange.ZERO) {
        })
    );
  }

}