package me.ialistannen.mimadebugger.machine.memory;

import org.immutables.value.Value;

/**
 * Represents all possible register values of the MiMa.
 */
@Value.Immutable
public abstract class Registers {

  /**
   * The accumulator register that temporary results will be stored in.
   *
   * @return the accumulator register.
   */
  @Value.Default
  public int accumulator() {
    return 0;
  }

  /**
   * The left input of the ALU.
   *
   * @return the left input of the ALU
   */
  @Value.Default
  public int aluInputLeft() {
    return 0;
  }

  /**
   * The right input of the ALU.
   *
   * @return the right input of the ALU.
   */
  @Value.Default
  public int aluInputRight() {
    return 0;
  }

  /**
   * The opcode of the current instruction.
   *
   * @return the opcode of the current instruction.
   */
  @Value.Default
  public int instruction() {
    return 0;
  }

  /**
   * The pointer to the next instruction.
   *
   * @return the pointer to the next instruction
   */
  @Value.Default
  public int instructionPointer() {
    return 0;
  }

  public ImmutableRegisters copy() {
    return ImmutableRegisters.copyOf(this);
  }
}
