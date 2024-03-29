package me.ialistannen.mimadebugger.util;

import static me.ialistannen.mimadebugger.StringUtil.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ThreadLocalRandom;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import org.junit.jupiter.api.Test;

class MemoryFormatTest {

  private String formatToString(int value) {
    return MemoryFormat.toString(value, 32, false);
  }

  @Test
  void testZeroToStringNoLeading() {
    assertThat(MemoryFormat.toString(0, 24, true)).isEqualTo("0");
  }

  @Test
  void testOneToStringNoLeading() {
    assertThat(MemoryFormat.toString(1, 24, true)).isEqualTo("01");
  }

  @Test
  void testMinusOneToStringNoLeading() {
    assertThat(MemoryFormat.toString(-1, 24, true)).isEqualTo(repeat("1", 24));
  }

  @Test
  void testZeroToString() {
    assertThat(formatToString(0)).isEqualTo(repeat("0", 32));
  }

  @Test
  void testOneToString() {
    assertThat(formatToString(1)).isEqualTo(repeat("0", 31) + "1");
  }

  @Test
  void testMinusOneToString() {
    assertThat(formatToString(-1)).isEqualTo(repeat("1", 32));
  }

  @Test
  void testMixedOneZeroToString() {
    assertThat(formatToString(698)).isEqualTo(repeat("0", 22) + "1010111010");
  }

  @Test
  void testMixedNegativeOneZeroToString() {
    assertThat(formatToString(-698)).isEqualTo(repeat("1", 22) + "0101000110");
  }

  @Test
  void testMaxValueToString() {
    assertThat(formatToString(Integer.MAX_VALUE)).isEqualTo("0" + repeat("1", 31));
  }

  @Test
  void testMinValueToString() {
    assertThat(formatToString(Integer.MIN_VALUE)).isEqualTo("1" + repeat("0", 31));
  }

  @Test
  void testCoerceZeroToAddress() throws NumberOverflowException {
    assertThat(MemoryFormat.coerceToAddress(0)).isEqualTo(0);
  }

  @Test
  void testCoerceFittingNumberToAddress() throws NumberOverflowException {
    int value = 20202;
    assertThat(MemoryFormat.coerceToAddress(value)).isEqualTo(value);
  }

  @Test
  void testCoerceMaximumNumberToAddress() throws NumberOverflowException {
    int value = (int) (Math.pow(2, MemoryFormat.ADDRESS_LENGTH) - 1);
    assertThat(MemoryFormat.coerceToAddress(value)).isEqualTo(value);
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
    assertThat(MemoryFormat.coerceToValue(0)).isEqualTo(0);
  }

  @Test
  void testCoerceMinusOneToValue() {
    assertThat(MemoryFormat.coerceToValue(-1)).isEqualTo(-1);
  }

  @Test
  void testCoerceFittingNumberToValue() {
    int value = 20202;
    assertThat(MemoryFormat.coerceToValue(value)).isEqualTo(value);
  }

  @Test
  void testCoerceMaximumNumberToValue() {
    int value = (int) (Math.pow(2, MemoryFormat.VALUE_LENGTH - 1) - 1);
    assertThat(MemoryFormat.coerceToValue(value)).isEqualTo(value);
  }

  @Test
  void testCoerceNegativeNumberToValue() {
    int value = -1;
    assertThat(MemoryFormat.coerceToValue(value)).isEqualTo(value);
  }

  @Test
  void testCoerceNegativeMinNumberToValue() {
    int value = (int) -Math.pow(2, MemoryFormat.VALUE_LENGTH - 1);
    assertThat(MemoryFormat.coerceToValue(value)).isEqualTo(value);
  }

  @Test
  void testCoerceBiggerNumberToValue() {
    assertThat(MemoryFormat.coerceToValue(2 << MemoryFormat.VALUE_LENGTH + 1)).isEqualTo(
        Math.floorMod(2 << MemoryFormat.VALUE_LENGTH + 1, 2 << MemoryFormat.VALUE_LENGTH));
  }

  @Test
  void testCoerceMaxPlusOne() {
    assertThat(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + 1)).isEqualTo(
        MemoryFormat.VALUE_MINIMUM);
  }

  @Test
  void testCoerceMinMinusOne() {
    assertThat(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MINIMUM - 1)).isEqualTo(
        MemoryFormat.VALUE_MAXIMUM);
  }

  @Test
  void testCoerceAddMinimum() {
    assertThat(MemoryFormat.coerceToValue(
        MemoryFormat.VALUE_MINIMUM + MemoryFormat.VALUE_MINIMUM)).isEqualTo(0);
  }

  @Test
  void testCoerceAddMaximum() {
    assertThat(MemoryFormat.coerceToValue(
        MemoryFormat.VALUE_MAXIMUM + MemoryFormat.VALUE_MAXIMUM)).isEqualTo(-2);
  }

  @Test
  void testGetBitZero() {
    int input = 0;

    for (int i = 0; i < Integer.SIZE; i++) {
      assertThat(MemoryFormat.getBit(input, i)).isEqualTo((byte) 0);
    }
  }

  @Test
  void testGetBitOne() {
    int input = 1;

    assertThat(MemoryFormat.getBit(input, 0)).isEqualTo((byte) 1);

    for (int i = 1; i < Integer.SIZE; i++) {
      assertThat(MemoryFormat.getBit(input, i)).isEqualTo((byte) 0);
    }
  }

  @Test
  void testGetBitAllSet() {
    int input = -1;

    for (int i = 0; i < Integer.SIZE; i++) {
      assertThat(MemoryFormat.getBit(input, i)).isEqualTo((byte) 1);
    }
  }

  @Test
  void testLastBitSet() {
    int input = 0b10000000000000000000000000000000;

    assertThat(MemoryFormat.getBit(input, Integer.SIZE - 1)).isEqualTo((byte) 1);

    for (int i = 0; i < Integer.SIZE - 1; i++) {
      assertThat(MemoryFormat.getBit(input, i)).isEqualTo((byte) 0);
    }
  }

  @Test
  void testSetOneToZero() {
    int input = 1;

    assertThat(MemoryFormat.setBit(input, 0, false)).isEqualTo(0);
  }

  @Test
  void testSetZeroToOne() {
    int input = 0;

    assertThat(MemoryFormat.setBit(input, 0, true)).isEqualTo(1);
  }

  @Test
  void testSetMostSignificant() {
    int input = 0;

    assertThat(MemoryFormat.setBit(input, Integer.SIZE - 1, true)).isEqualTo(Integer.MIN_VALUE);
  }

  @Test
  void testUnsetMostSignificant() {
    int input = Integer.MIN_VALUE;

    assertThat(MemoryFormat.setBit(input, Integer.SIZE - 1, false)).isEqualTo(0);
  }

  @Test
  void testRestoreComplement() {
    int input = ThreadLocalRandom.current().nextInt();
    int inverted = ~input;

    for (int i = 0; i < 32; i++) {
      inverted = MemoryFormat.setBit(inverted, i, MemoryFormat.getBit(input, i) == 1);
    }

    assertThat(inverted).isEqualTo(input);
  }


}
