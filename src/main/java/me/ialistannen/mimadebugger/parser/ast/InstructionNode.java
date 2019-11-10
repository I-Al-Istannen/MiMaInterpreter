package me.ialistannen.mimadebugger.parser.ast;

import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * A node representing a possible instruction.
 */
public class InstructionNode extends AbstractSyntaxTreeNode {

  private String instruction;

  public InstructionNode(String instruction, int address, StringReader reader,
      HalfOpenIntRange span) {
    super(address, reader, span);
    this.instruction = instruction;
  }

  /**
   * Returns the name of the instruction this node was created for.
   *
   * @return the name of the instruction this node was created for
   */
  public String getInstruction() {
    return instruction;
  }

  @Override
  public String toString() {
    return "InstructionNode<" + instruction + ", " +
        getChildren().stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "))
        + "(" + getAddress() + ")>";
  }
}
