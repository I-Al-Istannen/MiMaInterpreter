package me.ialistannen.mimadebugger.parser.validation;

import me.ialistannen.mimadebugger.util.HalfOpenIntRange;
import org.immutables.value.Value;

/**
 * A parsing problem.
 */
@Value.Immutable
public abstract class ParsingProblem {

  /**
   * The error message.
   *
   * @return the error message
   */
  public abstract String message();

  /**
   * Returns the position in the input that caused the problem.
   *
   * @return the position in the input that caused the problem.
   */
  public abstract HalfOpenIntRange approximateSpan();
}
