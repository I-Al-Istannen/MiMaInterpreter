package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;

public class Special {

  public static Instruction HALT = ImmutableInstruction.builder()
      .opcode(15)
      .name("HALT")
      .action((state, address) -> state)
      .build();


  public static List<Instruction> getInstructions() {
    return List.of(HALT);
  }

}
