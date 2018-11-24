package me.ialistannen.mimadebugger.machine.instructions;

import org.immutables.value.Value;

@Value.Immutable
public abstract class InstructionCall {

  /**
   * Returns the instruction to be executed in this call.
   *
   * @return the instruction to be executed in this call
   */
  public abstract Instruction command();

  /**
   * Returns the argument for the instruction.
   *
   * @return the argument for the instruction. Always 0 if {@link Instruction#hasArgument()} is
   * false
   */
  @Value.Default
  public int argument() {
    return 0;
  }

}
