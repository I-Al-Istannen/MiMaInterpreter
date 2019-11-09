package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Logical {

  private Logical() {
    //no instance
  }

  public static final Instruction AND = ImmutableInstruction.builder()
      .opcode(4)
      .name("AND")
      .description("Accumulator & memory[argument] -> Accumulator")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(
                      state.registers().accumulator() & state.memory().get(address)
                  ))
          )
      )
      .build();

  public static final Instruction OR = ImmutableInstruction.builder()
      .opcode(5)
      .name("OR")
      .description("Accumulator | memory[argument] -> Accumulator")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(
                      state.registers().accumulator() | state.memory().get(address)
                  ))
          )
      )
      .build();

  public static final Instruction XOR = ImmutableInstruction.builder()
      .opcode(6)
      .name("XOR")
      .description("Accumulator ^ memory[argument] -> Accumulator")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(
                      state.registers().accumulator() ^ state.memory().get(address)
                  ))
          )
      )
      .build();

  public static final Instruction NOT = ImmutableInstruction.builder()
      .opcode(0xF1)
      .name("NOT")
      .description("~Accumulator -> Accumulator")
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
