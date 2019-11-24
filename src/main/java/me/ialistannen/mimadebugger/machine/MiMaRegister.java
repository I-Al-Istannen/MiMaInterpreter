package me.ialistannen.mimadebugger.machine;

import java.util.function.BiFunction;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.Registers;

/**
 * A mima register.
 */
public enum MiMaRegister {
  INSTRUCTION_ADDRESS_REGISTER("IAR", ImmutableRegisters::withInstructionPointer),
  ACCUMULATOR("ACC", ImmutableRegisters::withAccumulator),
  STACK_POINTER("SP", ImmutableRegisters::withStackPointer),
  FRAME_POINTER("FP", ImmutableRegisters::withFramePointer),
  RETURN_ADDRESS_REGISTER("RA", ImmutableRegisters::withReturnAddress);

  private String abbreviation;
  private BiFunction<ImmutableRegisters, Integer, ImmutableRegisters> setter;

  MiMaRegister(String abbreviation,
      BiFunction<ImmutableRegisters, Integer, ImmutableRegisters> setter) {
    this.abbreviation = abbreviation;
    this.setter = setter;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  /**
   * Sets the register.
   *
   * @param registers the register to set
   * @param value the value to set it to
   * @return a copy of the input registers with the register set to the passed value
   */
  public Registers set(Registers registers, int value) {
    return setter.apply(ImmutableRegisters.copyOf(registers), value);
  }
}
