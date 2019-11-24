package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * An assembly directive that sets a literal.
 */
public class AssemblerDirectiveLit extends AbstractSyntaxTreeNode implements LiteralValueNode {

  private LiteralValueNode value;

  public AssemblerDirectiveLit(int address, StringReader reader, HalfOpenIntRange span,
      ConstantNode value) {
    super(address, reader, span);
    this.value = value;
    addChild(value);
  }

  @Override
  public int getValue() {
    return getValueNode().getValue();
  }

  public LiteralValueNode getValueNode() {
    return value;
  }
}
