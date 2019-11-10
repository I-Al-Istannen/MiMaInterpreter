package me.ialistannen.mimadebugger.parser.ast;

import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * Represents a label declaration or usage.
 */
public class LabelNode extends AbstractSyntaxTreeNode {

  private String name;
  private boolean declaration;

  public LabelNode(String name, boolean declaration, int address, StringReader reader,
      HalfOpenIntRange span) {
    super(address, reader, span);
    this.name = name;
    this.declaration = declaration;
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
   * Returns whether this is a label declaration or usage.
   *
   * @return whether this is a label declaration or usage
   */
  public boolean isDeclaration() {
    return declaration;
  }

  @Override
  public String toString() {
    return "LabelNode<" + name + ", " + getChildren().stream()
        .map(Object::toString)
        .collect(Collectors.joining(", "))
        + "(" + getAddress() + ")>";
  }
}
