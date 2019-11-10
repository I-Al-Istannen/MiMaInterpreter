package me.ialistannen.mimadebugger.util;

import java.util.Objects;

/**
 * A closed integer range.
 */
public class HalfOpenIntRange {

  public static HalfOpenIntRange ZERO = new HalfOpenIntRange(0, 0);

  private int start;
  private int end;

  /**
   * Creates a new closed int range.
   *
   * @param start the start value (inclusive)
   * @param end the end value (inclusive)
   */
  public HalfOpenIntRange(int start, int end) {
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
   * The end value (exclusive).
   *
   * @return the end value (exclusive)
   */
  public int getEnd() {
    return end;
  }

  /**
   * Returns the length of this range, i.e. {@code end - start}.
   *
   * @return the length of this range
   */
  public int getLength() {
    return end - start;
  }

  /**
   * Checks whether this range contains a given value.
   *
   * @param n the value to check for
   * @return true of the range contains the given value
   */
  public boolean contains(int n) {
    return n >= getStart() && n < getEnd();
  }

  /**
   * Checks whether this range overlaps another one.
   *
   * @param other the other range
   * @return true of the range overlaps another one
   */
  public boolean intersects(HalfOpenIntRange other) {
    boolean thisInOther = getStart() >= other.getStart() && getStart() < other.getEnd();
    boolean otherInThis = other.getStart() >= getStart() && other.getStart() < getEnd();

    return thisInOther || otherInThis;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HalfOpenIntRange that = (HalfOpenIntRange) o;
    return start == that.start &&
        end == that.end;
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }

  @Override
  public String toString() {
    return "ClosedIntRange{" +
        "start=" + start +
        ", end=" + end +
        '}';
  }
}
