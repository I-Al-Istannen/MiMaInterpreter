package me.ialistannen.mimadebugger.util;

import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;

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
    return (value & 0x00f00000) >>> 20;
  }

  /**
   * Extracts the value from a combined integer.
   *
   * @param value the positive value, 20 bits wide (first 4 are reserved for opcode)
   * @return the extracted value
   * @throws NumberOverflowException if the number was more than 24 bits wide
   */
  public static int extractArgument(int value) {
    return coerceToAddress(value & 0x000fffff);
  }

  /**
   * Combines the given opcode and its argument to a single integer that can be stored in the
   * memory.
   *
   * @param opcode the opcode
   * @param argument the argument for it
   * @return the combined instruction
   */
  public static int combineInstruction(int opcode, int argument) {
    int result = opcode << 20;
    result = result | argument;

    return result;
  }

  /**
   * Combines the given instruction in a single integer that can be stored in the memory.
   *
   * @param call the
   * @return the combined instruction
   */
  public static int combineInstruction(InstructionCall call) {
    return combineInstruction(call.command().opcode(), call.argument());
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

  /**
   * Returns the bit at the given position, counting from the least to the most significant.
   *
   * @param input the input number
   * @param position the position of the bit
   * @return the bit at this position
   */
  public static byte getBit(int input, int position) {
    return (byte) (input >>> position & 1);
  }

  /**
   * Returns the bit at the given position, counting from the least to the most significant.
   *
   * @param input the input number
   * @param position the position of the bit
   * @param set whether the bit at this position is set
   * @return the bit at this position
   */
  public static int setBit(int input, int position, boolean set) {
    if (set) {
      return input | 1 << position;
    }
    return input & ~(1 << position);
  }
}
