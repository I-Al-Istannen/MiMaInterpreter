package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import java.util.concurrent.ThreadLocalRandom;
import me.ialistannen.mimadebugger.machine.ImmutableState;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;

abstract class InstructionTest {

  protected State getState() {
    return ImmutableState.builder()
        .memory(MainMemory.create())
        .registers(
            ImmutableRegisters.builder()
                .build()
        )
        .build();
  }

  protected int getRandomValue() {
    return ThreadLocalRandom.current().nextInt(
        MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MAXIMUM
    );
  }

}
