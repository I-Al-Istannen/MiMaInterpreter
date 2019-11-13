package me.ialistannen.mimadebugger.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.parser.validation.ParsingProblem;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * A skeleton {@link SyntaxTreeNode}.
 */
public abstract class AbstractSyntaxTreeNode implements SyntaxTreeNode {

  private SyntaxTreeNode parent;
  private List<SyntaxTreeNode> children;
  private List<ParsingProblem> problems;
  private int address;
  private StringReader reader;
  private HalfOpenIntRange span;

  public AbstractSyntaxTreeNode(int address, StringReader reader, HalfOpenIntRange span) {
    this(Collections.emptyList(), address, reader, span);
  }

  public AbstractSyntaxTreeNode(List<SyntaxTreeNode> children, int address, StringReader reader,
      HalfOpenIntRange span) {
    this.children = new ArrayList<>(children);
    this.problems = new ArrayList<>();
    this.address = address;
    this.reader = reader;
    this.span = span;

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
  public HalfOpenIntRange getSpan() {
    return span;
  }

  @Override
  public List<ParsingProblem> getProblems() {
    return Collections.unmodifiableList(problems);
  }

  @Override
  public void addProblem(ParsingProblem problem) {
    problems.add(problem);
  }

  @Override
  public void accept(NodeVisitor visitor) {
    visitor.visit(this);
  }

}
