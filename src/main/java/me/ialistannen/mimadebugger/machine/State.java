package me.ialistannen.mimadebugger.machine;

import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.machine.memory.Registers;
import org.immutables.value.Value;

/**
 * The current state of the MiMa{@literal .} Immutable.
 */
@Value.Immutable
public abstract class State {

  /**
   * The current register values.
   *
   * @return the current register values
   */
  public abstract Registers registers();

  /**
   * The {@link MainMemory} of the MiMa.
   *
   * @return the main memory of the MiMa
   */
  public abstract MainMemory memory();

  public ImmutableState copy() {
    return ImmutableState.copyOf(this);
  }
}
