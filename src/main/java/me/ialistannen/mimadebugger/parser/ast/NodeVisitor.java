package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;

/**
 * A visitor implementation for the {@link SyntaxTreeNode}.
 */
public interface NodeVisitor {

  /**
   * Visits an {@link InstructionNode}
   *
   * @param instructionNode the instruction node
   * @throws MiMaSyntaxError if a syntax error is encountered
   */
  default void visitInstructionNode(InstructionNode instructionNode) throws MiMaSyntaxError {
  }

  /**
   * Visits a {@link LabelNode}.
   *
   * @param labelNode the label node
   * @throws MiMaSyntaxError if a syntax error is encountered
   */
  default void visitLabelNode(LabelNode labelNode) throws MiMaSyntaxError {
  }

  /**
   * Visits a {@link ConstantNode}.
   *
   * @param constantNode the constant node
   * @throws MiMaSyntaxError if a syntax error is encountered
   */
  default void visitConstantNode(ConstantNode constantNode) throws MiMaSyntaxError {
  }

  /**
   * Visits a {@link InstructionCallNode}.
   *
   * @param instructionCallNode the instruction call node
   * @throws MiMaSyntaxError if a syntax error is encountered
   */
  default void visitInstructionCallNode(InstructionCallNode instructionCallNode)
      throws MiMaSyntaxError {
  }

  /**
   * Visits the given node, dynamically dispatching to the right method and ignoring the root node.
   *
   * @param node the node to visit
   * @throws IllegalArgumentException if the node type was not known
   */
  default void visit(SyntaxTreeNode node) throws MiMaSyntaxError {
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
