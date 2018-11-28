package me.ialistannen.mimadebugger.parser.ast;

import java.util.List;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.parser.util.StringReader;

/**
 * The root node of the tree.
 */
public class RootNode extends AbstractSyntaxTreeNode {

  public RootNode(List<SyntaxTreeNode> children, StringReader reader) {
    super(children, 0, reader);
  }

  @Override
  public String toString() {
    return "RootNode<" + getChildren().stream()
        .map(Object::toString)
        .collect(Collectors.joining(", "))
        + ">";
  }
}
