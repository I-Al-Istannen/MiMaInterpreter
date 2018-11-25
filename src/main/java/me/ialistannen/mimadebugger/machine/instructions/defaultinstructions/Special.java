package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Collections;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;

public class Special {

  private Special() {
    //no instance
  }

  public static final Instruction HALT = ImmutableInstruction.builder()
      .opcode(15)
      .name("HALT")
      .hasArgument(false)
      .action((state, address) -> state)
      .build();


  public static List<Instruction> getInstructions() {
    return Collections.singletonList(HALT);
  }

}
