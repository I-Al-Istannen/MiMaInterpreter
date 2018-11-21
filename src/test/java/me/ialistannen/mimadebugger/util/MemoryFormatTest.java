package me.ialistannen.mimadebugger.util;

import static me.ialistannen.mimadebugger.util.StringUtil.repeat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    assertThrows(
        NumberOverflowException.class,
        () -> MemoryFormat.coerceToValue((int) (Math.pow(2, MemoryFormat.VALUE_LENGTH) + 1))
    );
  }
}