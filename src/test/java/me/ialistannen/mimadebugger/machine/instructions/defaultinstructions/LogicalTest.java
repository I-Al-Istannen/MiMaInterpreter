package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.AND;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.NOT;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.OR;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical.XOR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class LogicalTest extends InstructionTest {

  //<editor-fold desc="AND">
  @Test
  void testAndZeros() {
    assertThat(
        binaryOperatorResult(0, 0, AND),
        is(0)
    );
  }

  @Test
  void testAndWithItself() {
    int number = getRandomValue();
    assertThat(
        binaryOperatorResult(number, number, AND),
        is(number)
    );
  }

  @Test
  void testAndWithComplement() {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(
          binaryOperatorResult(number, ~number, AND),
          is(0)
      );
    }
  }

  @Test
  void testAndWithMinimum() {
    assertThat(
        binaryOperatorResult(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM, AND),
        is(MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testAndWithMaximum() {
    assertThat(
        binaryOperatorResult(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM, AND),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }

  @Test
  void testAndWithMinusOne() {
    assertThat(
        binaryOperatorResult(-1, -1, AND),
        is(-1)
    );
  }

  @Test
  void testAndWithGivenValue() {
    int first = 0b0001110101;
    int second = 0b11011010101;
    assertThat(
        binaryOperatorResult(first, second, AND),
        is(0b00001010101)
    );
  }

  @Test
  void testAndWithTooBigValue() {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(
        binaryOperatorResult(bigger, bigger, AND),
        is(MemoryFormat.coerceToValue(bigger))
    );
    assertThat(
        binaryOperatorResult(bigger, ~bigger, AND),
        is(0)
    );
  }

  //</editor-fold>

  //<editor-fold desc="OR">
  @Test
  void testOrZeros() {
    assertThat(
        binaryOperatorResult(0, 0, OR),
        is(0)
    );
  }

  @Test
  void testOrWithItself() {
    int number = getRandomValue();
    assertThat(
        binaryOperatorResult(number, number, OR),
        is(number)
    );
  }

  @Test
  void testOrWithComplement() {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(
          binaryOperatorResult(number, ~number, OR),
          is(-1)
      );
    }
  }

  @Test
  void testOrWithMinimum() {
    assertThat(
        binaryOperatorResult(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM, OR),
        is(MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testOrWithMaximum() {
    assertThat(
        binaryOperatorResult(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM, OR),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }

  @Test
  void testOrWithMinusOne() {
    assertThat(
        binaryOperatorResult(-1, -1, OR),
        is(-1)
    );
  }

  @Test
  void testOrWithGivenValue() {
    int first = 0b0001110101;
    int second = 0b11011010101;
    assertThat(
        binaryOperatorResult(first, second, OR),
        is(0b11011110101)
    );
  }

  @Test
  void testOrWithTooBigValue() {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(
        binaryOperatorResult(bigger, bigger, OR),
        is(0b11111111100000000000000000000000)
    );
    assertThat(
        binaryOperatorResult(bigger, ~bigger, OR),
        is(-1)
    );
  }

  //</editor-fold>

  //<editor-fold desc="XOR">
  @Test
  void testXorZeros() {
    assertThat(
        binaryOperatorResult(0, 0, XOR),
        is(0)
    );
  }

  @Test
  void testXorWithItself() {
    int number = getRandomValue();
    assertThat(
        binaryOperatorResult(number, number, XOR),
        is(0)
    );
  }

  @Test
  void testXorWithComplement() {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(
          binaryOperatorResult(number, ~number, XOR),
          is(-1)
      );
    }
  }

  @Test
  void testXorWithMinimum() {
    assertThat(
        binaryOperatorResult(MemoryFormat.VALUE_MINIMUM, MemoryFormat.VALUE_MINIMUM, XOR),
        is(0)
    );
  }

  @Test
  void testXorWithMaximum() {
    assertThat(
        binaryOperatorResult(MemoryFormat.VALUE_MAXIMUM, MemoryFormat.VALUE_MAXIMUM, XOR),
        is(0)
    );
  }

  @Test
  void testXorWithMinusOne() {
    assertThat(
        binaryOperatorResult(-1, -1, XOR),
        is(0)
    );
  }

  @Test
  void testXorWithGivenValue() {
    int first = 0b0001110101;
    int second = 0b11011010101;
    assertThat(
        binaryOperatorResult(first, second, XOR),
        is(0b11010100000)
    );
  }

  @Test
  void testXorWithTooBigValue() {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(
        binaryOperatorResult(bigger, bigger, XOR),
        is(0)
    );
    assertThat(
        binaryOperatorResult(bigger, ~bigger, XOR),
        is(-1)
    );
  }
  //</editor-fold>

  //<editor-fold desc="NOT">
  @Test
  void testNotZero() {
    assertThat(
        notResult(0),
        is(-1)
    );
  }

  @Test
  void testNotRandom() {
    for (int i = 0; i < 10_000; i++) {
      int number = getRandomValue();
      assertThat(
          notResult(number),
          is(~number)
      );
    }
  }

  @Test
  void testNotWithMinimum() {
    assertThat(
        notResult(MemoryFormat.VALUE_MINIMUM),
        is(~MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testNotWithMaximum() {
    assertThat(
        notResult(MemoryFormat.VALUE_MAXIMUM),
        is(~MemoryFormat.VALUE_MAXIMUM)
    );
  }

  @Test
  void testNotWithMinusOne() {
    assertThat(
        notResult(-1),
        is(0)
    );
  }

  @Test
  void testNotWithGivenValue() {
    int value = 0b0001110101;
    assertThat(
        notResult(value),
        is(0b11111111111111111111111110001010)
    );
  }

  @Test
  void testNotWithTooBigValue() {
    int bigger = MemoryFormat.VALUE_MAXIMUM + 1;
    // 100000000000000000000000 is the result after the overflow
    assertThat(
        notResult(bigger),
        is(0b00000000011111111111111111111111)
    );
  }
  //</editor-fold>

  private int binaryOperatorResult(int memory, int accumulator, Instruction instruction) {
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

  private int notResult(int accumulator) {
    State state = getState().copy()
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        );

    return NOT.apply(state, 0).registers().accumulator();
  }

}