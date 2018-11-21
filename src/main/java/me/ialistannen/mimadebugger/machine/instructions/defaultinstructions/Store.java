package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;

public class Store {

  public static final Instruction STORE = ImmutableInstruction.builder()
      .opcode(4)
      .name("STV")
      .action((state, address) -> state.copy()
          .withMemory(
              state.memory()
                  .set(address, state.registers().accumulator())
          )
      )
      .build();

  public static final Instruction STORE_INDIRECT = ImmutableInstruction.builder()
      .opcode(5)
      .name("STIV")
      .action((state, address) -> state.copy()
          .withMemory(
              state.memory()
                  .set(state.memory().get(address), state.registers().accumulator())
          )
      )
      .build();

  public static List<Instruction> getInstructions() {
    return List.of(STORE, STORE_INDIRECT);
  }
}
