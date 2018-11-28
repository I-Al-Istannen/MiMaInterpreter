package me.ialistannen.mimadebugger.parser.util;

import java.util.regex.Pattern;

public interface StringReader {

  /**
   * Returns the full input string.
   *
   * @return the full input string
   */
  String getString();

  /**
   * Returns the current cursor position.
   *
   * @return the current cursor position
   */
  int getCursor();

  /**
   * Returns whether there is any more input to read.
   *
   * @return whether there is any more input to read
   */
  boolean canRead();

  /**
   * Returns whether there is enough input to read {@code length} chars
   *
   * @param length the amount of characters to read
   * @return whether there is enough input to read {@code length} chars
   */
  boolean canRead(int length);

  /**
   * Returns the next {@code length} characters of input.
   *
   * @param length the length of the input
   * @return the next {@code length} lines of input
   */
  String peek(int length);

  /**
   * Checks if the next input matches the {@code expected} string.
   *
   * @param expected the expected string
   * @return true if the next input matches the {@code expected} string
   */
  boolean peek(String expected);

  /**
   * Checks if the next input matches the {@code expected} {@link Pattern}.
   *
   * @param expected the expected pattern
   * @return true if the next input matches the {@code expected} pattern
   */
  boolean peek(Pattern expected);

  /**
   * Returns a deep copy of this {@link StringReader}.
   *
   * @return the copy
   */
  StringReader copy();
}
