package me.ialistannen.mimadebugger.parser.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link StringReader} that can be modified.
 */
public class MutableStringReader implements StringReader {

  private String string;
  private int cursor;

  public MutableStringReader(String string) {
    this(string, 0);
  }

  private MutableStringReader(String string, int cursor) {
    this.string = string;
    this.cursor = cursor;
  }

  @Override
  public String getString() {
    return string;
  }

  @Override
  public int getCursor() {
    return cursor;
  }

  @Override
  public boolean canRead() {
    return cursor < string.length();
  }

  @Override
  public boolean canRead(int length) {
    return cursor + length < string.length();
  }

  @Override
  public boolean peek(String expected) {
    if (!canRead(expected.length())) {
      return false;
    }
    int oldPosition = cursor;
    boolean succeeded = read(expected.length()).equals(expected);
    cursor = oldPosition;

    return succeeded;
  }

  @Override
  public boolean peek(Pattern expected) {
    Matcher matcher = expected.matcher(string);
    return matcher.find(cursor) && matcher.start() == cursor;
  }

  @Override
  public String peek(int length) {
    int cursorPos = cursor;
    String read = read(length);
    cursor = cursorPos;
    return read;
  }

  /**
   * Reads the given amount of characters from the string, or less if nothing more is in it.
   *
   * @param length the length of the string to read
   * @return the read string
   */
  public String read(int length) {
    int start = cursor;
    int end = Math.min(cursor + length, string.length());

    cursor = end;

    return string.substring(start, end);
  }

  /**
   * Reads the match of the given pattern from the string.
   *
   * @param pattern the pattern to read
   * @return the read string or an empty string it didn't match
   */
  public String read(Pattern pattern) {
    int start = cursor;
    Matcher matcher = pattern.matcher(string);
    if (!matcher.find(cursor) || matcher.start() != cursor) {
      return "";
    }

    cursor = matcher.end();

    return string.substring(start, cursor);
  }

  @Override
  public StringReader copy() {
    return new MutableStringReader(string, cursor);
  }
}