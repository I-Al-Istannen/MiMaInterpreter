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
   * @return the argument for the instruction
   */
  public abstract int argument();

}
