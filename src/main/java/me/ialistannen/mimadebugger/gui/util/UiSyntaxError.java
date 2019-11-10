package me.ialistannen.mimadebugger.gui.util;

import java.util.Objects;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * A syntax error the UI can display.
 */
public class UiSyntaxError {

  private HalfOpenIntRange span;
  private String message;

  public UiSyntaxError(HalfOpenIntRange span, String message) {
    this.span = span;
    this.message = message;
  }

  /**
   * Returns the span that is affected by the error.
   *
   * @return the span that is affected by the error.
   */
  public HalfOpenIntRange getSpan() {
    return span;
  }

  /**
   * Returns the error message.
   *
   * @return the error message
   */
  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UiSyntaxError that = (UiSyntaxError) o;
    return Objects.equals(span, that.span) &&
        Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(span, message);
  }

  @Override
  public String toString() {
    return "UiSyntaxError{" +
        "span=" + span +
        ", message='" + message + '\'' +
        '}';
  }
}
