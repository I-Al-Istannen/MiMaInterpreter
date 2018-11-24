package me.ialistannen.mimadebugger.machine.instructions;

import java.util.function.BiFunction;
import me.ialistannen.mimadebugger.machine.State;
import org.immutables.value.Value;

/**
 * A single instruction for the MiMa.
 */
@Value.Immutable
public abstract class Instruction {

  /**
   * The opcode for this instruction.
   *
   * @return opcode for this instruction
   */
  public abstract int opcode();

  /**
   * The name of this instruction.
   *
   * @return the name of this instruction
   */
  public abstract String name();

  /**
   * Returns whether the instruction has any arguments.
   *
   * @return whether the instruction has any arguments
   */
  @Value.Default
  public boolean hasArgument() {
    return true;
  }

  /**
   * The action this instruction executes.
   *
   * @return the action this instruction executes
   */
  abstract BiFunction<State, Integer, State> action();

  /**
   * Applies this instruction to the given {@link State}.
   *
   * @param state the state to apply it to
   * @param argument the instruction argument
   * @return the resulting state
   */
  public State apply(State state, int argument) {
    return action().apply(state, argument);
  }
}
