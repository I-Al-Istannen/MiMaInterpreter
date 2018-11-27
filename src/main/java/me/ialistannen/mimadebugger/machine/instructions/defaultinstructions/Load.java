package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Load {

  private Load() {
    //no instance
  }

  public static final Instruction LOAD_CONSTANT = ImmutableInstruction.builder()
      .opcode(0)
      .name("LDC")
      .action((state, argument) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToAddress(argument))
          )
      )
      .build();

  public static final Instruction LOAD_FROM_ADDRESS = ImmutableInstruction.builder()
      .opcode(1)
      .name("LDV")
      .action((state, argument) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(state.memory().get(argument))
          )
      )
      .build();

  public static final Instruction LOAD_INDIRECT_FROM_ADDRESS = ImmutableInstruction.builder()
      .opcode(11)
      .name("LDIV")
      .action((state, argument) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(state.memory().get(state.memory().get(argument)))
          )
      )
      .build();

  public static List<Instruction> getInstructions() {
    return Arrays.asList(LOAD_CONSTANT, LOAD_FROM_ADDRESS, LOAD_INDIRECT_FROM_ADDRESS);
  }
}
