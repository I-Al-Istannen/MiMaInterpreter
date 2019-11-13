package me.ialistannen.mimadebugger.parser.ast;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.parser.validation.ImmutableParsingProblem;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * A node that could not be parsed.
 */
public class UnparsableNode extends AbstractSyntaxTreeNode {

  public UnparsableNode(int address, StringReader reader, HalfOpenIntRange span, String message) {
    super(address, reader, span);
    addProblem(ImmutableParsingProblem.builder()
        .message(message)
        .approximateSpan(span)
        .build()
    );
  }
}
