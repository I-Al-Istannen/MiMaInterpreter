package me.ialistannen.mimadebugger.parser.util;

/**
 * Allows performing checked actions unchecked.
 */
public class DoUnchecked {

  /**
   * Runs an action unchecked, rethrowing any exceptions.
   *
   * @param action the action to run
   * @param <T> the return type
   * @return the result
   */
  public static <T> T doIt(UncheckedYieldingRunnable<T> action) {
    try {
      return action.runFor();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * An unchecked runnable that yields a value.
   *
   * @param <T> the type of the result
   */
  public interface UncheckedYieldingRunnable<T> {

    T runFor() throws Exception;
  }
}
