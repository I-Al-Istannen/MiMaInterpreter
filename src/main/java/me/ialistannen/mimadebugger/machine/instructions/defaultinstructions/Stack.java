package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Stack {

  public static List<Instruction> getInstructions() {
    return Arrays.asList(
        LDRF, STRF, LDRS, STRS, LDSP, STSP, LDFP, STFP
    );
  }

  public static final Instruction LDRS = ImmutableInstruction.builder()
      .opcode(0xFA)
      .name("LDRS")
      .description("SP + argument -> Accumulator")
      .hasArgument(true)
      .argumentWidth(16)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy()
              .withAccumulator(state.memory().get(
                  state.registers().stackPointer() + MemoryFormat.coerceToValue(address)
              ))
      ))
      .build();

  public static final Instruction STRS = ImmutableInstruction.builder()
      .opcode(0xFB)
      .name("STRS")
      .description("Accumulator -> memory[SP + argument]")
      .hasArgument(true)
      .argumentWidth(16)
      .action((state, address) -> state.copy().withMemory(
          state.memory().set(
              state.registers().stackPointer() + MemoryFormat.coerceToValue(address),
              state.registers().accumulator()
          )
      ))
      .build();

  public static final Instruction LDRF = ImmutableInstruction.builder()
      .opcode(0xFC)
      .name("LDRF")
      .description("FP + argument -> Accumulator")
      .hasArgument(true)
      .argumentWidth(16)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy()
              .withAccumulator(state.memory().get(
                  state.registers().stackPointer() + MemoryFormat.coerceToValue(address)
              ))
      ))
      .build();

  public static final Instruction STRF = ImmutableInstruction.builder()
      .opcode(0xFD)
      .name("STRF")
      .description("Accumulator -> memory[FP + argument]")
      .hasArgument(true)
      .argumentWidth(16)
      .action((state, address) -> state.copy().withMemory(
          state.memory().set(
              state.registers().stackPointer() + MemoryFormat.coerceToValue(address),
              state.registers().accumulator()
          )
      ))
      .build();

  public static final Instruction LDSP = ImmutableInstruction.builder()
      .opcode(0xF6)
      .name("LDSP")
      .description("SP -> Accumulator")
      .hasArgument(false)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy().withAccumulator(
              state.registers().stackPointer()
          )
      ))
      .build();

  public static final Instruction STSP = ImmutableInstruction.builder()
      .opcode(0xF7)
      .name("STSP")
      .description("Accumulator -> SP")
      .hasArgument(false)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy().withStackPointer(
              MemoryFormat.coerceToAddress(state.registers().accumulator())
          )
      ))
      .build();

  public static final Instruction LDFP = ImmutableInstruction.builder()
      .opcode(0xF8)
      .name("LDFP")
      .description("FP -> Accumulator")
      .hasArgument(false)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy().withAccumulator(
              state.registers().framePointer()
          )
      ))
      .build();

  public static final Instruction STFP = ImmutableInstruction.builder()
      .opcode(0xF9)
      .name("STFP")
      .description("Accumulator -> FP")
      .hasArgument(false)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy().withFramePointer(
              MemoryFormat.coerceToAddress(state.registers().accumulator())
          )
      ))
      .build();

}
