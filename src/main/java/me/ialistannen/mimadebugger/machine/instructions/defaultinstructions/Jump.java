package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Jump {

  public static Instruction JUMP = ImmutableInstruction.builder()
      .opcode(12)
      .name("JUMP")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withInstructionPointer(MemoryFormat.coerceToAddress(address))
          )
      )
      .build();

  public static Instruction JUMP_IF_NEGATIVE = ImmutableInstruction.builder()
      .opcode(13)
      .name("JMN")
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withInstructionPointer(MemoryFormat.coerceToAddress(
                      state.registers().accumulator() < 0
                          ? address
                          : state.registers().instructionPointer()
                  ))
          )
      )
      .build();


  public static List<Instruction> getInstructions() {
    return List.of(JUMP, JUMP_IF_NEGATIVE);
  }

}
