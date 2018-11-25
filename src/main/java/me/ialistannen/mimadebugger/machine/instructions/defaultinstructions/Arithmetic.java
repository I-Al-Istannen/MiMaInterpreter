package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Arithmetic {

  public static final Instruction ADD = ImmutableInstruction.builder()
      .opcode(6)
      .name("ADD")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(
                      // Underflow properly
                      MemoryFormat.coerceToValue(
                          state.registers().accumulator() + state.memory().get(address)
                      )
                  )
                  .withAluInputLeft(state.registers().accumulator())
                  .withAluInputRight(state.memory().get(address))
          )
      )
      .build();


  public static List<Instruction> getInstructions() {
    return Arrays.asList(ADD);
  }
}
