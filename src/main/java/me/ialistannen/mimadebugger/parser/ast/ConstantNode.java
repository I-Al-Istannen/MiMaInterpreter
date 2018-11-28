package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.parser.util.StringReader;

/**
 * A node with a constant value.
 */
public class ConstantNode extends AbstractSyntaxTreeNode {

  private int value;

  public ConstantNode(int value, int address, StringReader reader) {
    super(address, reader);
    this.value = value;
  }

  /**
   * Returns the value of this node.
   *
   * @return the value of this node
   */
  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "ConstantNode<" + value + "(" + getAddress() + ")" + '>';
  }
}
