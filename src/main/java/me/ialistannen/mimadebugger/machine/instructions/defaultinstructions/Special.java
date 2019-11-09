package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Collections;
import java.util.List;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;

public class Special {

  private Special() {
    //no instance
  }

  public static final Instruction HALT = ImmutableInstruction.builder()
      .opcode(0xF0)
      .name("HALT")
      .description("Stops execution")
      .hasArgument(false)
      .action((state, address) -> {
        throw new ProgramHaltException();
      })
      .build();


  public static List<Instruction> getInstructions() {
    return Collections.singletonList(HALT);
  }

}
