package me.ialistannen.mimadebugger.exceptions;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

public class MiMaSyntaxError extends MiMaException {

  private static final int CONTEXT_AMOUNT = 10;
  private final String originalMessage;
  private final StringReader reader;
  private final HalfOpenIntRange span;

  /**
   * Creates a new MiMa syntax error.
   *
   * @param message the error message
   * @param reader the string reader with the full input
   * @param span the character span of the error in the input. What this is is not specified,
   *     but it should be related to the failing token(s) and not empty. It will be used for
   *     displaying a context.
   */
  public MiMaSyntaxError(String message, StringReader reader, HalfOpenIntRange span) {
    super(message + " at '" + getContext(reader, span) + "'");

    this.originalMessage = message;
    this.reader = reader.copy();
    this.span = span;
  }

  private static String getContext(StringReader reader, HalfOpenIntRange span) {
    String fullInput = reader.getString();

    int start;
    int end;
    String beforeMarker;

    if (span.getLength() == 0) {
      start = Math.max(0, Math.min(fullInput.length(), reader.getCursor() - CONTEXT_AMOUNT));
      end = reader.getCursor();
    } else {
      start = span.getStart();
      end = span.getEnd();
    }
    beforeMarker = fullInput.substring(start, end);

    if (start != 0) {
      beforeMarker = "..." + beforeMarker;
    }

    return beforeMarker + "<--[HERE]";
  }

  /**
   * Returns the original error message without context.
   *
   * @return the original error message
   */
  public String getOriginalMessage() {
    return originalMessage;
  }

  /**
   * Returns the character span of the error in the input. What this is is not specified, but it
   * should be related to the failing token(s) and not empty.
   *
   * @return the character span of the error in the input
   */
  public HalfOpenIntRange getSpan() {
    return span;
  }

  /**
   * Returns the relevant string reader.
   *
   * @return the relevant string reader.
   */
  public StringReader getReader() {
    return reader;
  }
}
