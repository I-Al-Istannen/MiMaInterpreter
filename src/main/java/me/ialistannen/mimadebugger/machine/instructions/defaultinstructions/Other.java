package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.Collections;
import java.util.List;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstruction;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class Other {

  private Other() {
    //no instance
  }

  public static final Instruction ROTATE_RIGHT = ImmutableInstruction.builder()
      .name("RAR")
      .opcode(0xF2)
      .hasArgument(false)
      .action((state, ignored) -> {
        int accumulator = state.registers().accumulator();
        int result = rotateRight(accumulator);

        return state.copy()
            .withRegisters(
                state.registers().copy()
                    .withAccumulator(result)
            );
      })
      .build();

  public static List<Instruction> getInstructions() {
    return Collections.singletonList(ROTATE_RIGHT);
  }

  static int rotateRight(int value) {
    int result = value >>> 1;
    result = MemoryFormat.coerceToValue(result);
    result = MemoryFormat.setBit(
        result,
        MemoryFormat.VALUE_LENGTH - 1,
        MemoryFormat.getBit(value, 0) == 1
    );
    return result;
  }
}
