package me.ialistannen.mimadebugger.parser.processing;

import java.util.Optional;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.validation.ImmutableParsingProblem;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class ConstantVerification {

  private static final int MAX_ADDRESS = 1 << MemoryFormat.ADDRESS_LENGTH - 1;

  private InstructionSet instructionSet;

  public ConstantVerification(InstructionSet instructionSet) {
    this.instructionSet = instructionSet;
  }

  /**
   * Validates that all constants have an appropriate size and do not overflow.
   *
   * @param root the root node to check all children for
   */
  public void validateConstants(SyntaxTreeNode root) {
    root.accept(new NodeVisitor() {
      @Override
      public void visitConstantNode(ConstantNode constantNode) {
        validateSize(constantNode.getParent().orElse(null), constantNode);
      }
    });
  }

  private void validateSize(SyntaxTreeNode parent, ConstantNode node) {
    int value = node.getValue();
    if (parent instanceof InstructionNode) {
      String instructionName = ((InstructionNode) parent).getInstruction();
      Optional<Instruction> instruction = instructionSet.forName(instructionName);

      if (!instruction.isPresent()) {
        return;
      }

      int maximumValue = 1 << instruction.get().argumentWidth() - 1;
      boolean tooSmall = value < -maximumValue;
      boolean tooLarge = value > maximumValue;
      if (tooSmall || tooLarge) {
        node.addProblem(ImmutableParsingProblem.builder()
            .message(String.format("Address not in (%s, %s)", -maximumValue, maximumValue))
            .approximateSpan(node.getSpan())
            .build()
        );
      }
    } else if (value < MemoryFormat.VALUE_MINIMUM || value > MemoryFormat.VALUE_MAXIMUM) {
      node.addProblem(ImmutableParsingProblem.builder()
          .message("Value too small or too large (24 bit)")
          .approximateSpan(node.getSpan())
          .build()
      );
    }
  }
}
