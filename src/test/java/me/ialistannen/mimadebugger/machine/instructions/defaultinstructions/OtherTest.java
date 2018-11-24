package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class OtherTest {

  @Test
  void testRotateOne() {
    int input = 1;

    assertThat(
        Other.rotateRight(input),
        is(1 << MemoryFormat.VALUE_LENGTH - 1)
    );
  }

  @Test
  void testRotateZero() {
    int input = 0;

    assertThat(
        Other.rotateRight(input),
        is(0)
    );
  }

  @Test
  void testRotateMaximumValue() {
    int input = 0b00000000_011111111111111111111111;

    assertThat(
        Other.rotateRight(input),
        is(0b00000000_101111111111111111111111)
    );
  }

  @Test
  void testRotateMinimumValue() {
    int input = 0b00000000_111111111111111111111111;

    assertThat(
        Other.rotateRight(input),
        is(input)
    );
  }

}