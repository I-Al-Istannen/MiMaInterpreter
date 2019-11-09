package me.ialistannen.mimadebugger.exceptions;

import me.ialistannen.mimadebugger.parser.util.StringReader;

public class MiMaSyntaxError extends MiMaException {

  private static final int CONTEXT_AMOUNT = 10;
  private StringReader reader;

  public MiMaSyntaxError(String message, StringReader reader) {
    super(message + " at '" + getContext(reader) + "'");
    this.reader = reader.copy();
  }

  private static String getContext(StringReader reader) {
    int cursor = reader.getCursor();
    String fullInput = reader.getString();

    int start = Math.max(0, Math.min(fullInput.length(), cursor - CONTEXT_AMOUNT));
    String beforeMarker = fullInput.substring(start, cursor);

    if (start != 0) {
      beforeMarker = "..." + beforeMarker;
    }

    return beforeMarker + "<--[HERE]";
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
