package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.machine.MiMaRegister;
import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

public class AssemblerDirectiveRegister extends AbstractSyntaxTreeNode {

  private MiMaRegister register;
  private LiteralValueNode value;

  private AssemblerDirectiveRegister(int address, StringReader reader, HalfOpenIntRange span,
      MiMaRegister register, LiteralValueNode value) {
    super(address, reader, span);
    this.register = register;
    this.value = value;
  }

  public LiteralValueNode getValue() {
    return value;
  }

  public MiMaRegister getRegister() {
    return register;
  }

  public static <T extends LiteralValueNode & SyntaxTreeNode> AssemblerDirectiveRegister of(
      int address, StringReader reader, HalfOpenIntRange span, MiMaRegister register, T value) {

    AssemblerDirectiveRegister node = new AssemblerDirectiveRegister(
        address, reader, span, register, value
    );

    node.addChild(value);

    return node;
  }
}
