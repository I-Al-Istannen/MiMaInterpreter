package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Functions {

  public static List<Instruction> getInstructions() {
    return Arrays.asList(
        CALL, RET, LDRA, STRA
    );
  }


  public static final Instruction CALL = ImmutableInstruction.builder()
      .opcode(0xC)
      .name("CALL")
      .description("IAR -> RA ; argument -> IAR")
      .hasArgument(true)
      .action((state, address) ->
          state.copy().withRegisters(
              state.registers().copy()
                  .withReturnAddress(state.registers().instructionPointer())
                  .withInstructionPointer(MemoryFormat.coerceToAddress(address))
          )
      )
      .build();

  public static final Instruction RET = ImmutableInstruction.builder()
      .opcode(0xF3)
      .name("RET")
      .description("RA -> IAR")
      .hasArgument(false)
      .action((state, address) ->
          state.copy().withRegisters(
              state.registers().copy()
                  .withInstructionPointer(MemoryFormat.coerceToAddress(
                      state.registers().returnAddress()
                  ))
          )
      )
      .build();

  public static final Instruction LDRA = ImmutableInstruction.builder()
      .opcode(0xF4)
      .name("LDRA")
      .description("RA -> Accumulator")
      .hasArgument(false)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy().withAccumulator(
              state.registers().returnAddress()
          )
      ))
      .build();

  public static final Instruction STRA = ImmutableInstruction.builder()
      .opcode(0xF5)
      .name("STRA")
      .description("Accumulator -> RA")
      .hasArgument(false)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy().withReturnAddress(
              MemoryFormat.coerceToAddress(state.registers().accumulator())
          )
      ))
      .build();
}
