package me.ialistannen.mimadebugger.parser.ast;

/**
 * A visitor implementation for the {@link SyntaxTreeNode}.
 */
public interface NodeVisitor {

  /**
   * Visits an {@link InstructionNode}
   *
   * @param instructionNode the instruction node
   */
  default void visitInstructionNode(InstructionNode instructionNode) {
  }

  /**
   * Visits a {@link LabelNode}.
   *
   * @param labelNode the label node
   */
  default void visitLabelNode(LabelNode labelNode) {
  }

  /**
   * Visits a {@link ConstantNode}.
   *
   * @param constantNode the constant node
   */
  default void visitConstantNode(ConstantNode constantNode) {
  }

  /**
   * Visits a {@link InstructionCallNode}.
   *
   * @param instructionCallNode the instruction call node
   */
  default void visitInstructionCallNode(InstructionCallNode instructionCallNode) {
  }

  /**
   * Visits the given node, dynamically dispatching to the right method and ignoring the root node.
   *
   * @param node the node to visit
   * @throws IllegalArgumentException if the node type was not known
   */
  default void visit(SyntaxTreeNode node) {
    if (node instanceof LabelNode) {
      visitLabelNode((LabelNode) node);
    } else if (node instanceof InstructionNode) {
      visitInstructionNode((InstructionNode) node);
    } else if (node instanceof ConstantNode) {
      visitConstantNode((ConstantNode) node);
    } else if (node instanceof InstructionCallNode) {
      visitInstructionCallNode((InstructionCallNode) node);
    } else if (node instanceof RootNode) {
      // ignore root
    } else {
      throw new IllegalArgumentException("Unknown node type: " + node);
    }
  }
}
