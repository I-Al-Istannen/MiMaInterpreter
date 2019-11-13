package me.ialistannen.mimadebugger.parser.ast;

/**
 * A node that stores a literal Value, e.g. a constant or an address
 */
public interface LiteralValueNode {

  /**
   * Returns the literal value (e.g. constant) of this node.
   *
   * @return the literal value of this node
   */
  int getValue();
}
