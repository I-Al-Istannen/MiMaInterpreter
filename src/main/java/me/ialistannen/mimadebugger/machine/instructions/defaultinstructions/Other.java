package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Other {

  private Other() {
    //no instance
  }

  public static List<Instruction> getInstructions() {
    return Arrays.asList(
        ROTATE_RIGHT, ADC
    );
  }

  public static final Instruction ROTATE_RIGHT = ImmutableInstruction.builder()
      .name("RAR")
      .opcode(0xF2)
      .description("Accumulator rotated right by argument -> Accumulator")
      .hasArgument(false)
      .action((state, ignored) -> {
        int accumulator = state.registers().accumulator();
        int result = rotateRight(accumulator);

        return state.copy().withRegisters(
            state.registers().copy().withAccumulator(result)
        );
      })
      .build();

  private static int rotateRight(int value) {
    int result = value >>> 1;
    result = MemoryFormat.coerceToValue(result);
    result = MemoryFormat.setBit(
        result,
        MemoryFormat.VALUE_LENGTH - 1,
        MemoryFormat.getBit(value, 0) == 1
    );
    return result;
  }


  public static final Instruction ADC = ImmutableInstruction.builder()
      .opcode(0xFA)
      .name("ADC")
      .description("Accumulator + argument -> Accumulator")
      .hasArgument(true)
      .action((state, address) -> state.copy().withRegisters(
          state.registers().copy().withAccumulator(
              state.registers().accumulator() + MemoryFormat.coerceToLargeOpcodeArgument(address)
          )
      ))
      .build();
}
