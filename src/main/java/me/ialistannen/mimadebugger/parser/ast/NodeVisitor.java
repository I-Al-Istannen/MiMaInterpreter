package me.ialistannen.mimadebugger.parser.ast;

import java.util.ArrayList;

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
    visitChildren(instructionNode);
  }

  /**
   * Visits a {@link LabelDeclarationNode}.
   *
   * @param labelNode the label node
   */
  default void visitLabelDeclarationNode(LabelDeclarationNode labelNode) {
    visitChildren(labelNode);
  }

  /**
   * Visits a {@link LabelUsageNode}.
   *
   * @param labelNode the label node
   */
  default void visitLabelUsageNode(LabelUsageNode labelNode) {
    visitChildren(labelNode);
  }

  /**
   * Visits a {@link ConstantNode}.
   *
   * @param constantNode the constant node
   */
  default void visitConstantNode(ConstantNode constantNode) {
    visitChildren(constantNode);
  }

  /**
   * Visits a {@link InstructionCallNode}.
   *
   * @param instructionCallNode the instruction call node
   */
  default void visitInstructionCallNode(InstructionCallNode instructionCallNode) {
    visitChildren(instructionCallNode);
  }

  /**
   * Visits a {@link CommentNode}.
   *
   * @param commentNode the comment node
   */
  default void visitCommentNode(CommentNode commentNode) {
    visitChildren(commentNode);
  }

  /**
   * Visits a {@link LiteralNode}.
   *
   * @param literalNode the literal node
   */
  default void visitLiteralNode(LiteralNode literalNode) {
    visitChildren(literalNode);
  }

  /**
   * Visits a {@link AssemblerDirectiveOrigin}.
   *
   * @param Node the origin node
   */
  default void visitAssemblerDirectiveOrigin(AssemblerDirectiveOrigin Node) {
    visitChildren(Node);
  }

  /**
   * Visits a {@link AssemblerDirectiveLit}.
   *
   * @param Node the literal node
   */
  default void visitAssemblerDirectiveLit(AssemblerDirectiveLit Node) {
    visitChildren(Node);
  }

  /**
   * Visits a {@link AssemblerDirectiveRegister}.
   *
   * @param Node the register node
   */
  default void visitAssemblerDirectiveRegister(AssemblerDirectiveRegister Node) {
    visitChildren(Node);
  }

  /**
   * Visits an {@link UnparsableNode}.
   *
   * @param unparsableNode the unparsable node
   */
  default void visitUnparsableNode(UnparsableNode unparsableNode) {
    visitChildren(unparsableNode);
  }

  default void visitChildren(SyntaxTreeNode node) {
    for (SyntaxTreeNode child : new ArrayList<>(node.getChildren())) {
      child.accept(this);
    }
  }

  /**
   * Visits the given node, dynamically dispatching to the right method and ignoring the root node.
   *
   * @param node the node to visit
   * @throws IllegalArgumentException if the node type was not known
   */
  default void visit(SyntaxTreeNode node) {
    if (node instanceof LabelUsageNode) {
      visitLabelUsageNode((LabelUsageNode) node);
    } else if (node instanceof LabelDeclarationNode) {
      visitLabelDeclarationNode((LabelDeclarationNode) node);
    } else if (node instanceof InstructionNode) {
      visitInstructionNode((InstructionNode) node);
    } else if (node instanceof ConstantNode) {
      visitConstantNode((ConstantNode) node);
    } else if (node instanceof InstructionCallNode) {
      visitInstructionCallNode((InstructionCallNode) node);
    } else if (node instanceof CommentNode) {
      visitCommentNode((CommentNode) node);
    } else if (node instanceof RootNode) {
      visitChildren(node);
    } else if (node instanceof UnparsableNode) {
      visitUnparsableNode((UnparsableNode) node);
    } else if (node instanceof LiteralNode) {
      visitLiteralNode((LiteralNode) node);
    } else if (node instanceof AssemblerDirectiveRegister) {
      visitAssemblerDirectiveRegister((AssemblerDirectiveRegister) node);
    } else if (node instanceof AssemblerDirectiveLit) {
      visitAssemblerDirectiveLit((AssemblerDirectiveLit) node);
    } else if (node instanceof AssemblerDirectiveOrigin) {
      visitAssemblerDirectiveOrigin((AssemblerDirectiveOrigin) node);
    } else {
      throw new IllegalArgumentException("Unknown node type: " + node);
    }
  }
}
