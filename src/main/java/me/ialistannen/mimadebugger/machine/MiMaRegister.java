package me.ialistannen.mimadebugger.machine;

import java.util.function.BiFunction;
import java.util.function.Function;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.Registers;

/**
 * A mima register.
 */
public enum MiMaRegister {
  INSTRUCTION_ADDRESS_REGISTER(
      "IAR", ImmutableRegisters::withInstructionPointer, Registers::instructionPointer
  ),
  ACCUMULATOR("ACC", ImmutableRegisters::withAccumulator, Registers::accumulator),
  STACK_POINTER("SP", ImmutableRegisters::withStackPointer, Registers::stackPointer),
  FRAME_POINTER("FP", ImmutableRegisters::withFramePointer, Registers::framePointer),
  RETURN_ADDRESS_REGISTER("RA", ImmutableRegisters::withReturnAddress, Registers::returnAddress);

  private String abbreviation;
  private BiFunction<ImmutableRegisters, Integer, ImmutableRegisters> setter;
  private Function<Registers, Integer> getter;

  MiMaRegister(String abbreviation,
      BiFunction<ImmutableRegisters, Integer, ImmutableRegisters> setter,
      Function<Registers, Integer> getter) {
    this.abbreviation = abbreviation;
    this.setter = setter;
    this.getter = getter;
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

  /**
   * Returns the value of this register in the given Registers block.
   *
   * @param registers the source
   * @return the value
   */
  public int get(Registers registers) {
    return getter.apply(registers);
  }
}
