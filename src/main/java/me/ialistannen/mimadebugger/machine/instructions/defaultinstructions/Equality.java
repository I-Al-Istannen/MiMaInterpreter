package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Collections;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Equality {

  private Equality() {
    //no instance
  }

  public static final Instruction EQUAL = ImmutableInstruction.builder()
      .opcode(7)
      .name("EQL")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(
                      state.registers().accumulator() == state.memory().get(address) ? -1 : 0
                  ))
          )
      )
      .build();


  public static List<Instruction> getInstructions() {
    return Collections.singletonList(EQUAL);
  }

}
