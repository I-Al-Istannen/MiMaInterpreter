package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.parser.util.StringReader;

public class InstructionCallNode extends AbstractSyntaxTreeNode {

  private InstructionCall instructionCall;

  public InstructionCallNode(int address, StringReader reader, InstructionCall call) {
    super(address, reader);
    this.instructionCall = call;
  }

  /**
   * Returns the stored {@link InstructionCall};
   *
   * @return the stored instruction call
   */
  public InstructionCall getInstructionCall() {
    return instructionCall;
  }

  @Override
  public String toString() {
    return "InstructionCallNode<" + instructionCall + "(" + getAddress() + ")>";
  }
}
