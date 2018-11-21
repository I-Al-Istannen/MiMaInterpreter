package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

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
      .action((state, address) -> state.copy()
          .withRegisters(
              state.registers().copy()
                  .withAccumulator(MemoryFormat.coerceToValue(~state.registers().accumulator()))
          )
      )
      .build();


  public static List<Instruction> getInstructions() {
    return List.of(AND, OR, XOR);
  }
}
