package me.ialistannen.mimadebugger.parser.util;

import me.ialistannen.mimadebugger.exceptions.MiMaException;

/**
 * A runnable that might throw a {@link MiMaException}.
 */
public interface MiMaExceptionRunnable {

  /**
   * Runs the action, maybe throwing a {@link MiMaException}.
   *
   * @throws MiMaException if it decides to
   */
  void run() throws MiMaException;

}
