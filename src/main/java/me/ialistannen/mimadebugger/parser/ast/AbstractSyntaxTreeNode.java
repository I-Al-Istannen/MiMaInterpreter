package me.ialistannen.mimadebugger.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.parser.util.StringReader;

/**
 * A skeleton {@link SyntaxTreeNode}.
 */
public abstract class AbstractSyntaxTreeNode implements SyntaxTreeNode {

  private SyntaxTreeNode parent;
  private List<SyntaxTreeNode> children;
  private int address;
  private StringReader reader;

  public AbstractSyntaxTreeNode(int address, StringReader reader) {
    this(Collections.emptyList(), address, reader);
  }

  public AbstractSyntaxTreeNode(List<SyntaxTreeNode> children, int address, StringReader reader) {
    this.children = new ArrayList<>(children);
    this.address = address;
    this.reader = reader;

    this.children.forEach(child -> child.setParent(this));
  }

  @Override
  public void addChild(SyntaxTreeNode child) {
    children.add(child);
    child.setParent(this);
  }

  @Override
  public void removeChild(SyntaxTreeNode child) {
    children.remove(child);
    child.setParent(null);
  }

  @Override
  public int getAddress() {
    return address;
  }

  @Override
  public List<SyntaxTreeNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public Optional<SyntaxTreeNode> getParent() {
    return Optional.ofNullable(parent);
  }

  @Override
  public void setParent(SyntaxTreeNode parent) {
    this.parent = parent;
  }

  @Override
  public StringReader getStringReader() {
    return reader;
  }

  @Override
  public void accept(NodeVisitor visitor) throws MiMaSyntaxError {
    visitor.visit(this);
    for (SyntaxTreeNode node : new ArrayList<>(getChildren())) {
      node.accept(visitor);
    }
  }

}
