package me.ialistannen.mimadebugger.parser.ast;

import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * Represents a label declaration or usage.
 */
public class LabelDeclarationNode extends AbstractSyntaxTreeNode implements LiteralValueNode {

  private String name;

  public LabelDeclarationNode(String name, int address, StringReader reader,
      HalfOpenIntRange span) {
    super(address, reader, span);
    this.name = name;
  }

  /**
   * The name of the label.
   *
   * @return the name of the label
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the address this label points to. An alias for {@link #getAddress()}.
   *
   * @return the address this label points to
   */
  @Override
  public int getValue() {
    return getAddress();
  }

  @Override
  public String toString() {
    return "LabelDeclarationNode<" + name + ", " + getChildren().stream()
        .map(Object::toString)
        .collect(Collectors.joining(", "))
        + "(" + getAddress() + ")>";
  }
}
