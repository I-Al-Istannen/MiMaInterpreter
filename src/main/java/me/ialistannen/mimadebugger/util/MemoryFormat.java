package me.ialistannen.mimadebugger.util;

import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;

public class MemoryFormat {

  public static final int ADDRESS_LENGTH = 20;
  public static final int VALUE_LENGTH = 24;
  public static final int VALUE_MAXIMUM = (int) (Math.pow(2, 24 - 1) - 1);
  public static final int VALUE_MINIMUM = (int) -Math.pow(2, 24);

  /**
   * Coerces the value to the size of an address ({@value ADDRESS_LENGTH}).
   *
   * @param value the value
   * @return the value with all leading ignored bits zeroed out.
   * @throws NumberOverflowException if the number overflows
   */
  public static int coerceToAddress(int value) {
    if (value < 0) {
      throw new NumberOverflowException(value, ADDRESS_LENGTH);
    }
    int masked = value & 0xfffff;

    if (masked != value) {
      throw new NumberOverflowException(value, ADDRESS_LENGTH);
    }

    return masked;
  }

  /**
   * Coarces the value to the size of a value ({@value VALUE_LENGTH}).
   *
   * @param value the value
   * @return the value with all leading bits set to zeros or ones (depending on the sign)
   * @throws NumberOverflowException if the number overflows
   */
  public static int coerceToValue(int value) {
    // cut off first 8 bits
    int masked = value & 0x00ffffff;

    if (value > VALUE_MAXIMUM || value < VALUE_MINIMUM) {
      throw new NumberOverflowException(value, VALUE_LENGTH);
    }

    if (value >= 0) {
      return masked;
    }

    // sign expansion on first 8 bits
    masked = masked | 0xff000000;

    return masked;
  }

  /**
   * Extracts the opcode from a combined integer.
   *
   * @param value the value, 24 bits wide
   * @return the extracted opcode
   * @throws NumberOverflowException if the number was more than 24 bits wide
   */
  public static int extractOpcode(int value) {
    return (coerceToValue(value) & 0x00f00000) >>> 20;
  }

  /**
   * Extracts the value from a combined integer.
   *
   * @param value the value, 24 bits wide
   * @return the extracted value
   * @throws NumberOverflowException if the number was more than 24 bits wide
   */
  public static int extractArgument(int value) {
    return coerceToValue(value) & 0x000fffff;
  }

  public static int combineInstruction(int opcode, int value) {
    int result = opcode << 20;
    result = result | value;

    return result;
  }

  /**
   * Converts an int to its string representation.
   *
   * @param value the value to convert
   * @param length the length of the output
   * @param skipLeadingZeros whether to skip leading zeros
   * @return the value as a string
   */
  public static String toString(int value, int length, boolean skipLeadingZeros) {
    StringBuilder result = new StringBuilder();

    for (int i = length - 1; i >= 0; i--) {
      result.append(getBit(value, i));
    }

    if (skipLeadingZeros) {
      String output = result.toString();
      int firstOne = output.indexOf("1");
      if (firstOne < 0) {
        return "0";
      }
      if (firstOne == 0) {
        return output;
      }
      return "0" + output.substring(firstOne);
    }

    return result.toString();
  }

  private static byte getBit(int input, int bitNumber) {
    return (byte) (input >>> bitNumber & 1);
  }
}
