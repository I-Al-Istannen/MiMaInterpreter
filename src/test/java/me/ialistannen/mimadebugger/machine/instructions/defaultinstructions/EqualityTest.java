package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.assertj.core.api.Assertions.assertThat;

import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class EqualityTest extends InstructionTest {

  private static final int EQUAL = -1;
  private static final int DIFFERENT = 0;

  @Test
  void testEqual() throws MiMaException {
    assertThat(isEqual(20, 20)).isEqualTo(EQUAL);
  }

  @Test
  void testNotEqual() throws MiMaException {
    assertThat(isEqual(21, 20)).isEqualTo(DIFFERENT);
  }

  @Test
  void testZero() throws MiMaException {
    assertThat(isEqual(0, 0)).isEqualTo(EQUAL);
  }

  @Test
  void testMinimum() throws MiMaException {
    assertThat(isEqual(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM)).isEqualTo(EQUAL);
  }

  @Test
  void testMaximum() throws MiMaException {
    assertThat(isEqual(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM)).isEqualTo(EQUAL);
  }

  @Test
  void testRandom() throws MiMaException {
    for (int i = 0; i < 10_000; i++) {
      int accum = getRandomValue();
      int memory = getRandomValue();
      assertThat(isEqual(accum, memory))
          .isEqualTo((accum == memory ? EQUAL : DIFFERENT));
    }
  }

  private int isEqual(int accumulator, int memory) throws MiMaException {
    State state = getState().copy()
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        )
        .withMemory(
            MainMemory.create().set(0, memory)
        );

    return Equality.EQUAL.apply(state, 0).registers().accumulator();
  }
}
