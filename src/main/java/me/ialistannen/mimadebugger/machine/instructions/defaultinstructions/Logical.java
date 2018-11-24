package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Logical {

  public static Instruction AND = ImmutableInstruction.builder()
      .opcode(7)
      .name("AND")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(
                      state.registers().accumulator() & state.memory().get(address)
                  ))
          )
      )
      .build();

  public static Instruction OR = ImmutableInstruction.builder()
      .opcode(8)
      .name("OR")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(
                      state.registers().accumulator() | state.memory().get(address)
                  ))
          )
      )
      .build();

  public static Instruction XOR = ImmutableInstruction.builder()
      .opcode(9)
      .name("AND")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(
                      state.registers().accumulator() ^ state.memory().get(address)
                  ))
          )
      )
      .build();

  public static Instruction NOT = ImmutableInstruction.builder()
      .opcode(10)
      .name("NOT")
      .hasArgument(false)
      .action((state, ignored) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(not(state.registers().accumulator()))
          )
      )
      .build();


  private static int not(int value) {
    int masked = MemoryFormat.maskToValue(~value);

    // expand the sign if the complement is negative, to preserve it on 32 bits
    if (MemoryFormat.getBit(masked, MemoryFormat.VALUE_LENGTH - 1) == 0) {
      return MemoryFormat.coerceToValue(masked);
    }

    return MemoryFormat.coerceToValue(~value);
  }

  public static List<Instruction> getInstructions() {
    return Arrays.asList(AND, OR, XOR, NOT);
  }
}
