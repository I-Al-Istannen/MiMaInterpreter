package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class ArithmeticTest extends InstructionTest {

  @Test
  void testAddZeros() throws MiMaException {
    assertThat(
        add(0, 0),
        is(0)
    );
  }

  @Test
  void testAddOnes() throws MiMaException {
    assertThat(
        add(1, 1),
        is(2)
    );
  }

  @Test
  void testAddMinusOne() throws MiMaException {
    assertThat(
        add(-1, -1),
        is(-2)
    );
  }

  @Test
  void testAddOverflowMaximum() throws MiMaException {
    assertThat(
        add(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM),
        is(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + MemoryFormat.VALUE_MAXIMUM))
    );
  }

  @Test
  void testOverflowMinimumPlusOne() throws MiMaException {
    assertThat(
        add(MemoryFormat.VALUE_MINIMUM, -1),
        is(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM))
    );
  }

  @Test
  void testAddMinimums() throws MiMaException {
    assertThat(
        add(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM),
        is(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MINIMUM + MemoryFormat.VALUE_MINIMUM))
    );
  }

  @Test
  void testAddRandomNumbers() throws MiMaException {
    for (int i = 0; i < 10_000; i++) {
      int numberOne = getRandomValue();
      int numberTwo = getRandomValue();

      assertThat(
          add(numberOne, numberTwo),
          is(MemoryFormat.coerceToValue(numberOne + numberTwo))
      );
    }
  }

  private int add(int memory, int accumulator) throws MiMaException {
    State initialState = getState();
    State state = initialState.copy()
        .withMemory(
            initialState.memory().set(1, memory)
        ).withRegisters(
            initialState.registers().copy()
                .withAccumulator(accumulator)
        );

    State result = Arithmetic.ADD.apply(state, 1);
    return result.registers().accumulator();
  }
}