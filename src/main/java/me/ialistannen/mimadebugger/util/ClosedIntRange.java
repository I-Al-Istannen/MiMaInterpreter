package me.ialistannen.mimadebugger.util;

/**
 * A closed integer range.
 */
public class ClosedIntRange {

  public static ClosedIntRange ZERO = new ClosedIntRange(0, 0);

  private int start;
  private int end;

  /**
   * Creates a new closed int range.
   *
   * @param start the start value (inclusive)
   * @param end the end value (inclusive)
   */
  public ClosedIntRange(int start, int end) {
    this.start = start;
    this.end = end;
  }

  /**
   * The start value (inclusive).
   *
   * @return the start value (inclusive)
   */
  public int getStart() {
    return start;
  }

  /**
   * The end value (inclusive).
   *
   * @return the end value (inclusive)
   */
  public int getEnd() {
    return end;
  }
}
