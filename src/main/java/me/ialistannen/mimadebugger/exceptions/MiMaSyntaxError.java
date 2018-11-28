package me.ialistannen.mimadebugger.exceptions;

import me.ialistannen.mimadebugger.parser.util.StringReader;

public class MiMaSyntaxError extends MiMaException {

  private static final int CONTEXT_AMOUNT = 10;

  public MiMaSyntaxError(String message, StringReader reader) {
    super(message + " at '" + getContext(reader) + "'");
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
}
