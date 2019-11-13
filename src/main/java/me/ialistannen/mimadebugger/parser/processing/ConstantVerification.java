package me.ialistannen.mimadebugger.parser.processing;

import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.validation.ImmutableParsingProblem;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class ConstantVerification {

  private static final int MAX_ADDRESS = 1 << MemoryFormat.ADDRESS_LENGTH - 1;

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
      if (value < 0 || value > MAX_ADDRESS) {
        node.addProblem(ImmutableParsingProblem.builder()
            .message("Address negative or larger than " + MAX_ADDRESS)
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
