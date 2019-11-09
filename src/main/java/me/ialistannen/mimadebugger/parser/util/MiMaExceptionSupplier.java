package me.ialistannen.mimadebugger.parser.util;

import me.ialistannen.mimadebugger.exceptions.MiMaException;

/**
 * A supplier that might throw a {@link MiMaException}.
 */
public interface MiMaExceptionSupplier<T> {

  /**
   * Returns the value, maybe throwing a {@link MiMaException}.
   *
   * @throws MiMaException if it decides to
   */
  T get() throws MiMaException;

}
