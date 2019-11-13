package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

public class LiteralNode extends AbstractSyntaxTreeNode implements LiteralValueNode {

  private final ConstantNode child;

  public LiteralNode(int address, StringReader reader, HalfOpenIntRange span, ConstantNode child) {
    super(address, reader, span);
    this.child = child;
    addChild(child);
  }

  @Override
  public int getValue() {
    return child.getValue();
  }
}
