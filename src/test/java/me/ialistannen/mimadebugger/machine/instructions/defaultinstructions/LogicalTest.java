package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.AND;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.NOT;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.OR;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.XOR;
import static org.assertj.core.api.Assertions.assertThat;

import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class LogicalTest extends InstructionTest {

  //<editor-fold desc="AND">
  @Test
  void testAndZeros() throws MiMaException {
    assertThat(binaryOperatorResult(0, 0, AND))
        .isEqualTo(0);
  }

  @Test
  void testAndWithItself() throws MiMaException {
    int number = getRandomValue();
    assertThat(binaryOperatorResult(number, number, AND))
        .isEqualTo(number);
  }

  @Test
  void testAndWithComplement() throws MiMaException {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(binaryOperatorResult(number, ~number, AND))
          .isEqualTo(0);
    }
  }

  @Test
  void testAndWithMinimum() throws MiMaException {
    assertThat(binaryOperatorResult(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM, AND))
        .isEqualTo(MemoryFormat.VALUE_MINIMUM);
  }

  @Test
  void testAndWithMaximum() throws MiMaException {
    assertThat(binaryOperatorResult(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM, AND))
        .isEqualTo(MemoryFormat.VALUE_MAXIMUM);
  }

  @Test
  void testAndWithMinusOne() throws MiMaException {
    assertThat(binaryOperatorResult(-1, -1, AND))
        .isEqualTo(-1);
  }

  @Test
  void testAndWithGivenValue() throws MiMaException {
    int first = 0b0001110101;
    int second = 0b11011010101;
    assertThat(binaryOperatorResult(first, second, AND))
        .isEqualTo(0b00001010101);
  }

  @Test
  void testAndWithTooBigValue() throws MiMaException {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(binaryOperatorResult(bigger, bigger, AND))
        .isEqualTo(MemoryFormat.coerceToValue(bigger));
    assertThat(binaryOperatorResult(bigger, ~bigger, AND))
        .isEqualTo(0);
  }

  //</editor-fold>

  //<editor-fold desc="OR">
  @Test
  void testOrZeros() throws MiMaException {
    assertThat(binaryOperatorResult(0, 0, OR))
        .isEqualTo(0);
  }

  @Test
  void testOrWithItself() throws MiMaException {
    int number = getRandomValue();
    assertThat(binaryOperatorResult(number, number, OR))
        .isEqualTo(number);
  }

  @Test
  void testOrWithComplement() throws MiMaException {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(binaryOperatorResult(number, ~number, OR))
          .isEqualTo(-1);
    }
  }

  @Test
  void testOrWithMinimum() throws MiMaException {
    assertThat(binaryOperatorResult(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM, OR))
        .isEqualTo(MemoryFormat.VALUE_MINIMUM);
  }

  @Test
  void testOrWithMaximum() throws MiMaException {
    assertThat(binaryOperatorResult(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM, OR))
        .isEqualTo(MemoryFormat.VALUE_MAXIMUM);
  }

  @Test
  void testOrWithMinusOne() throws MiMaException {
    assertThat(binaryOperatorResult(-1, -1, OR))
        .isEqualTo(-1);
  }

  @Test
  void testOrWithGivenValue() throws MiMaException {
    int first = 0b0001110101;
    int second = 0b11011010101;
    assertThat(binaryOperatorResult(first, second, OR))
        .isEqualTo(0b11011110101);
  }

  @Test
  void testOrWithTooBigValue() throws MiMaException {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(binaryOperatorResult(bigger, bigger, OR))
        .isEqualTo(0b11111111100000000000000000000000);
    assertThat(binaryOperatorResult(bigger, ~bigger, OR))
        .isEqualTo(-1);
  }

  //</editor-fold>

  //<editor-fold desc="XOR">
  @Test
  void testXorZeros() throws MiMaException {
    assertThat(binaryOperatorResult(0, 0, XOR))
        .isEqualTo(0);
  }

  @Test
  void testXorWithItself() throws MiMaException {
    int number = getRandomValue();
    assertThat(binaryOperatorResult(number, number, XOR))
        .isEqualTo(0);
  }

  @Test
  void testXorWithComplement() throws MiMaException {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(binaryOperatorResult(number, ~number, XOR))
          .isEqualTo(-1);
    }
  }

  @Test
  void testXorWithMinimum() throws MiMaException {
    assertThat(binaryOperatorResult(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM, XOR))
        .isEqualTo(0);
  }

  @Test
  void testXorWithMaximum() throws MiMaException {
    assertThat(binaryOperatorResult(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM, XOR))
        .isEqualTo(0);
  }

  @Test
  void testXorWithMinusOne() throws MiMaException {
    assertThat(binaryOperatorResult(-1, -1, XOR))
        .isEqualTo(0);
  }

  @Test
  void testXorWithGivenValue() throws MiMaException {
    int first = 0b0001110101;
    int second = 0b11011010101;
    assertThat(binaryOperatorResult(first, second, XOR))
        .isEqualTo(0b11010100000);
  }

  @Test
  void testXorWithTooBigValue() throws MiMaException {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(binaryOperatorResult(bigger, bigger, XOR))
        .isEqualTo(0);
    assertThat(binaryOperatorResult(bigger, ~bigger, XOR))
        .isEqualTo(-1);
  }
  //</editor-fold>

  //<editor-fold desc="NOT">
  @Test
  void testNotZero() throws MiMaException {
    assertThat(notResult(0))
        .isEqualTo(-1);
  }

  @Test
  void testNotRandom() throws MiMaException {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(notResult(number))
          .isEqualTo(~number);
    }
  }

  @Test
  void testNotWithMinimum() throws MiMaException {
    assertThat(notResult(MemoryFormat.VALUE_MINIMUM))
        .isEqualTo(~MemoryFormat.VALUE_MINIMUM);
  }

  @Test
  void testNotWithMaximum() throws MiMaException {
    assertThat(notResult(MemoryFormat.VALUE_MAXIMUM))
        .isEqualTo(~MemoryFormat.VALUE_MAXIMUM);
  }

  @Test
  void testNotWithMinusOne() throws MiMaException {
    assertThat(notResult(-1))
        .isEqualTo(0);
  }

  @Test
  void testNotWithGivenValue() throws MiMaException {
    int value = 0b0001110101;
    assertThat(notResult(value))
        .isEqualTo(0b11111111111111111111111110001010);
  }

  @Test
  void testNotWithTooBigValue() throws MiMaException {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(notResult(bigger))
        .isEqualTo(0b00000000011111111111111111111111);
  }
  //</editor-fold>

  private int binaryOperatorResult(int memory, int accumulator, Instruction instruction)
      throws MiMaException {
    State state = getState().copy()
        .withMemory(
            MainMemory.create()
                .set(0, memory)
        )
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        );

    return instruction.apply(state, 0).registers().accumulator();
  }

  private int notResult(int accumulator) throws MiMaException {
    State state = getState().copy()
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        );

    return NOT.apply(state, 0).registers().accumulator();
  }

}
