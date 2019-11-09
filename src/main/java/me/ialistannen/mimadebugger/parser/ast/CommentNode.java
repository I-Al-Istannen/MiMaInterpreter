package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.ClosedIntRange;

/**
 * A comment node.
 */
public class CommentNode extends AbstractSyntaxTreeNode {

  private String comment;

  public CommentNode(int address, StringReader reader, ClosedIntRange span, String comment) {
    super(address, reader, span);
    this.comment = comment;
  }

  /**
   * Returns the comment.
   *
   * @return the comment
   */
  public String getComment() {
    return comment;
  }

  @Override
  public String toString() {
    return "CommentNode{" +
        "comment='" + comment + '\'' +
        '}';
  }
}
