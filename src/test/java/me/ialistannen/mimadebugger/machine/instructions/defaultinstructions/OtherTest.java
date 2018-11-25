package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class OtherTest extends InstructionTest {

  @Test
  void testRotateOne() {
    int input = 1;

    assertThat(
        rotateRight(input),
        is(1 << MemoryFormat.VALUE_LENGTH - 1)
    );
  }

  @Test
  void testRotateZero() {
    int input = 0;

    assertThat(
        rotateRight(input),
        is(0)
    );
  }

  @Test
  void testRotateMaximumValue() {
    int input = 0b00000000_011111111111111111111111;

    assertThat(
        rotateRight(input),
        is(0b00000000_101111111111111111111111)
    );
  }

  @Test
  void testRotateMinimumValue() {
    int input = 0b00000000_111111111111111111111111;

    assertThat(
        rotateRight(input),
        is(input)
    );
  }

  private int rotateRight(int input) {
    State state = getState().copy()
        .withRegisters(
            getState().registers().copy().withAccumulator(input)
        );

    return Other.ROTATE_RIGHT.apply(state, 0).registers().accumulator();
  }

}