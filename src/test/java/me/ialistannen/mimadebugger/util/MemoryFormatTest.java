package me.ialistannen.mimadebugger.util;

import static me.ialistannen.mimadebugger.StringUtil.repeat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ThreadLocalRandom;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import org.junit.jupiter.api.Test;

class MemoryFormatTest {

  private String formatToString(int value) {
    return MemoryFormat.toString(value, 32, false);
  }

  @Test
  void testZeroToString() {
    assertThat(
        formatToString(0),
        is(repeat("0", 32))
    );
  }

  @Test
  void testOneToString() {
    assertThat(
        formatToString(1),
        is(repeat("0", 31) + "1")
    );
  }

  @Test
  void testMinusOneToString() {
    assertThat(
        formatToString(-1),
        is(repeat("1", 32))
    );
  }

  @Test
  void testMixedOneZeroToString() {
    assertThat(
        formatToString(698),
        is(repeat("0", 22) + "1010111010")
    );
  }

  @Test
  void testMixedNegativeOneZeroToString() {
    assertThat(
        formatToString(-698),
        is(repeat("1", 22) + "0101000110")
    );
  }

  @Test
  void testMaxValueToString() {
    assertThat(
        formatToString(Integer.MAX_VALUE),
        is("0" + repeat("1", 31))
    );
  }

  @Test
  void testMinValueToString() {
    assertThat(
        formatToString(Integer.MIN_VALUE),
        is("1" + repeat("0", 31))
    );
  }

  @Test
  void testCoerceZeroToAddress() {
    assertThat(
        MemoryFormat.coerceToAddress(0),
        is(0)
    );
  }

  @Test
  void testCoerceFittingNumberToAddress() {
    int value = 20202;
    assertThat(
        MemoryFormat.coerceToAddress(value),
        is(value)
    );
  }

  @Test
  void testCoerceMaximumNumberToAddress() {
    int value = (int) (Math.pow(2, MemoryFormat.ADDRESS_LENGTH) - 1);
    assertThat(
        MemoryFormat.coerceToAddress(value),
        is(value)
    );
  }

  @Test
  void testCoerceNegativeNumberToAddress() {
    assertThrows(NumberOverflowException.class, () -> MemoryFormat.coerceToAddress(-1));
  }

  @Test
  void testCoerceBiggerNumberToAddress() {
    assertThrows(
        NumberOverflowException.class,
        () -> MemoryFormat.coerceToAddress((int) (Math.pow(2, MemoryFormat.ADDRESS_LENGTH) + 1))
    );
  }

  @Test
  void testCoerceZeroToValue() {
    assertThat(
        MemoryFormat.coerceToValue(0),
        is(0)
    );
  }

  @Test
  void testCoerceFittingNumberToValue() {
    int value = 20202;
    assertThat(
        MemoryFormat.coerceToValue(value),
        is(value)
    );
  }

  @Test
  void testCoerceMaximumNumberToValue() {
    int value = (int) (Math.pow(2, MemoryFormat.VALUE_LENGTH - 1) - 1);
    assertThat(
        MemoryFormat.coerceToValue(value),
        is(value)
    );
  }

  @Test
  void testCoerceNegativeNumberToValue() {
    int value = -1;
    assertThat(
        MemoryFormat.coerceToValue(value),
        is(value)
    );
  }

  @Test
  void testCoerceNegativeMinNumberToValue() {
    int value = (int) -Math.pow(2, MemoryFormat.VALUE_LENGTH - 1);
    assertThat(
        MemoryFormat.coerceToValue(value),
        is(value)
    );
  }

  @Test
  void testCoerceBiggerNumberToValue() {
    assertThat(
        MemoryFormat.coerceToValue(2 << MemoryFormat.VALUE_LENGTH + 1),
        is(Math.floorMod(2 << MemoryFormat.VALUE_LENGTH + 1, 2 << MemoryFormat.VALUE_LENGTH))
    );
  }

  @Test
  void testCoerceMaxPlusOne() {
    assertThat(
        MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + 1),
        is(MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testCoerceMinMinusOne() {
    assertThat(
        MemoryFormat.coerceToValue(MemoryFormat.VALUE_MINIMUM - 1),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }

  @Test
  void testCoerceAddMinimum() {
    assertThat(
        MemoryFormat.coerceToValue(MemoryFormat.VALUE_MINIMUM + MemoryFormat.VALUE_MINIMUM),
        is(0)
    );
  }

  @Test
  void testCoerceAddMaximum() {
    assertThat(
        MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + MemoryFormat.VALUE_MAXIMUM),
        is(-2)
    );
  }

  @Test
  void testGetBitZero() {
    int input = 0;

    for (int i = 0; i < Integer.SIZE; i++) {
      assertThat(
          MemoryFormat.getBit(input, i),
          is((byte) 0)
      );
    }
  }

  @Test
  void testGetBitOne() {
    int input = 1;

    assertThat(
        MemoryFormat.getBit(input, 0),
        is((byte) 1)
    );

    for (int i = 1; i < Integer.SIZE; i++) {
      assertThat(
          MemoryFormat.getBit(input, i),
          is((byte) 0)
      );
    }
  }

  @Test
  void testGetBitAllSet() {
    int input = -1;

    for (int i = 0; i < Integer.SIZE; i++) {
      assertThat(
          MemoryFormat.getBit(input, i),
          is((byte) 1)
      );
    }
  }

  @Test
  void testLastBitSet() {
    int input = 0b10000000000000000000000000000000;

    assertThat(
        MemoryFormat.getBit(input, Integer.SIZE - 1),
        is((byte) 1)
    );

    for (int i = 0; i < Integer.SIZE - 1; i++) {
      assertThat(
          MemoryFormat.getBit(input, i),
          is((byte) 0)
      );
    }
  }

  @Test
  void testSetOneToZero() {
    int input = 1;

    assertThat(
        MemoryFormat.setBit(input, 0, false),
        is(0)
    );
  }

  @Test
  void testSetZeroToOne() {
    int input = 0;

    assertThat(
        MemoryFormat.setBit(input, 0, true),
        is(1)
    );
  }

  @Test
  void testSetMostSignificant() {
    int input = 0;

    assertThat(
        MemoryFormat.setBit(input, Integer.SIZE - 1, true),
        is(Integer.MIN_VALUE)
    );
  }

  @Test
  void testUnsetMostSignificant() {
    int input = Integer.MIN_VALUE;

    assertThat(
        MemoryFormat.setBit(input, Integer.SIZE - 1, false),
        is(0)
    );
  }

  @Test
  void testRestoreComplement() {
    int input = ThreadLocalRandom.current().nextInt();
    int inverted = ~input;

    for (int i = 0; i < 32; i++) {
      inverted = MemoryFormat.setBit(inverted, i, MemoryFormat.getBit(input, i) == 1);
    }

    assertThat(
        inverted,
        is(input)
    );
  }


}