package me.ialistannen.mimadebugger.parser.ast;

import java.util.List;
import java.util.Optional;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * A single node in the parsed syntax tree.
 */
public interface SyntaxTreeNode {

  /**
   * Returns the parent node.
   *
   * @return the parent node
   */
  Optional<SyntaxTreeNode> getParent();

  /**
   * Sets the parent node.
   *
   * @param node the parent node
   */
  void setParent(SyntaxTreeNode node);

  /**
   * Returns a modifiable list with all children of this Ast node.
   *
   * @return a modifiable list with all children of this Ast node
   */
  List<SyntaxTreeNode> getChildren();


  /**
   * Adds a child to this node.
   *
   * @param child the child to add
   */
  void addChild(SyntaxTreeNode child);

  /**
   * Removes a child from this node.
   *
   * @param child the child to remove
   */
  void removeChild(SyntaxTreeNode child);

  /**
   * Returns the address of this node.
   *
   * @return the address of this node
   */
  int getAddress();

  /**
   * Returns the string reader taken directly after this node was parsed.
   *
   * @return the string reader
   */
  StringReader getStringReader();

  /**
   * Returns the tokens this node spans.
   *
   * @return the tokens this node spans
   */
  HalfOpenIntRange getSpan();


  /**
   * Accepts a visitor and applies it to all child nodes.
   *
   * @param visitor the visitor
   * @throws MiMaSyntaxError if the visitor throws it
   */
  void accept(NodeVisitor visitor) throws MiMaSyntaxError;
}
