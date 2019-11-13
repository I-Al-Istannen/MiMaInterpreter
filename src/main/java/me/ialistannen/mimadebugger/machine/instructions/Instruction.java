package me.ialistannen.mimadebugger.machine.instructions;

import java.util.Optional;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
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
   * Returns< the bit length of the argument. Only valid if {@link #hasArgument()} is true.
   *
   * @return the bit length of the argument
   */
  @Value.Default
  public int argumentWidth() {
    return 20;
  }

  /**
   * A short description of the instruction.
   *
   * @return a short description of the instruction.
   */
  public abstract Optional<String> description();

  /**
   * The action this instruction executes.
   *
   * @return the action this instruction executes
   */
  abstract InstructionAction action();

  /**
   * Applies this instruction to the given {@link State}.
   *
   * @param state the state to apply it to
   * @param argument the instruction argument
   * @return the resulting state
   * @throws MiMaException if an error occurs
   */
  public State apply(State state, int argument) throws MiMaException {
    return action().apply(state, argument);
  }

  /**
   * Executes an instruction.
   */
  public interface InstructionAction {

    /**
     * Applies this function to the given arguments.
     *
     * @param state the current state
     * @param address the argument or address
     * @return the new state
     */
    State apply(State state, Integer address) throws MiMaException;
  }
}
