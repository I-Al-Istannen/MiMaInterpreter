package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * An assembler directive that sets the origin.
 */
public class AssemblerDirectiveOrigin extends AbstractSyntaxTreeNode {

  public AssemblerDirectiveOrigin(ConstantNode value, int address, StringReader reader,
      HalfOpenIntRange span) {
    super(address, reader, span);
    addChild(value);
  }
}
