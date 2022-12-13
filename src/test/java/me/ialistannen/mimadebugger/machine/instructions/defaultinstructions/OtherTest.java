package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OtherTest extends InstructionTest {

  @Test
  void testRotateOne() throws MiMaException {
    int input = 1;

    assertThat(rotateRight(input)).isEqualTo(1 << MemoryFormat.VALUE_LENGTH - 1);
  }

  @Test
  void testRotateZero() throws MiMaException {
    int input = 0;

    assertThat(rotateRight(input)).isEqualTo(0);
  }

  @Test
  void testRotateMaximumValue() throws MiMaException {
    int input = 0b00000000_011111111111111111111111;

    assertThat(rotateRight(input)).isEqualTo(0b00000000_101111111111111111111111);
  }

  @Test
  void testRotateMinimumValue() throws MiMaException {
    int input = 0b00000000_111111111111111111111111;

    assertThat(rotateRight(input)).isEqualTo(input);
  }

  private int rotateRight(int input) throws MiMaException {
    State state = getState().copy()
        .withRegisters(
            getState().registers().copy().withAccumulator(input)
        );

    return Other.ROTATE_RIGHT.apply(state, 0).registers().accumulator();
  }

  @ParameterizedTest(name = "Adding {1} to {0} to get {2}")
  @CsvSource({
      "20, 0, 20",
      "8388607, 1, 8388608",    // no overflow
      "-8388608, -1, -8388609", // no underflow
      "-8388608, 1, -8388607",  // add to min
      "8388607, -1, 8388606",   // sub from max
      "0, 1, 1",                // add to zero
      "0, -1, -1",              // sub from zero
      "0, 32767, 32767",        // add max
      "0, -32767, -32767",      // sub max
      "0, -32768, -32768",      // sub largest negative
  })
  void addFewConstants(int accumulator, int addition, int result) throws MiMaException {
    assertThat(addConstant(accumulator, addition)).isEqualTo(result);
  }

  @Test
  void overflowAddConstant() {
    assertThrows(
        NumberOverflowException.class,
        () -> addConstant(0, 0xFFFF) // signed number, so that is too large
    );
  }


  private int addConstant(int accumulatorBefore, int param) throws MiMaException {
    State state = getState().copy().withRegisters(
        getState().registers().copy().withAccumulator(accumulatorBefore)
    );

    return Other.ADC.apply(state, param).registers().accumulator();
  }

}
