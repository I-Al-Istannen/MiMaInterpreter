package me.ialistannen.mimadebugger.parser.ast;

import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * Represents a label declaration or usage.
 */
public class LabelUsageNode extends AbstractSyntaxTreeNode implements LiteralValueNode {

  private String name;
  private int referencedAddress;

  public LabelUsageNode(String name, int address, StringReader reader, HalfOpenIntRange span) {
    super(address, reader, span);
    this.name = name;
    this.referencedAddress = -1;
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
   * Sets the address this label points to.
   *
   * @param referencedAddress the address this label points to
   */
  public void setReferencedAddress(int referencedAddress) {
    this.referencedAddress = referencedAddress;
  }

  /**
   * Returns the address this label points to. An alias for {@link #getReferencedAddress()}.
   *
   * @return the address this label points to. -1 if not set
   */
  @Override
  public int getValue() {
    return referencedAddress;
  }

  /**
   * Returns the address this label points to.
   *
   * @return the address this label points to. -1 if not set
   */
  public int getReferencedAddress() {
    return referencedAddress;
  }

  @Override
  public String toString() {
    return "LabelNode<" + name + ", " + getChildren().stream()
        .map(Object::toString)
        .collect(Collectors.joining(", "))
        + "(" + getAddress() + "->" + getValue() + ")>";
  }
}
