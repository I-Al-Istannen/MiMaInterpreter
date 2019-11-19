package me.ialistannen.mimadebugger.util;

import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;

public class MemoryFormat {

  public static final int ADDRESS_LENGTH = 20;
  public static final int VALUE_LENGTH = 24;
  public static final int VALUE_MAXIMUM = (int) (Math.pow(2, VALUE_LENGTH - 1) - 1);
  public static final int VALUE_MINIMUM = (int) -(Math.pow(2, VALUE_LENGTH - 1));

  private MemoryFormat() {
    //no instance
  }

  /**
   * Coerces the value to the size of an address ({@value ADDRESS_LENGTH}).
   *
   * @param value the value
   * @return the value with all leading ignored bits zeroed out.
   * @throws NumberOverflowException if the number overflows
   */
  public static int coerceToAddress(int value) throws NumberOverflowException {
    // TODO: Error here or just cut off?
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
   * Coerces the value to the size of a value ({@value VALUE_LENGTH}), calculating what the overflow
   * would be, if necessary.
   *
   * @param value the value
   * @return the value
   */
  public static int coerceToValue(int value) {
    if (value <= VALUE_MAXIMUM && value >= VALUE_MINIMUM) {
      return value;
    }

    int fixed = value & 0x00ffffff;

    if (getBit(fixed, VALUE_LENGTH - 1) == 0) {
      return fixed;
    }

    return fixed | 0xff000000;
  }

  /**
   * Coerces the value to a 16 bit value as that is what a large opcode can take, overflowing when
   * necessary.
   *
   * <p>Prohibits overflows.</p>
   *
   * @param value the value
   * @return the value
   * @throws NumberOverflowException if the value is too large
   */
  public static int coerceToLargeOpcodeArgument(int value) throws NumberOverflowException {
    if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
      return value;
    }
    throw new NumberOverflowException(value, Short.SIZE);
  }

  /**
   * Mask the given value by cutting off all leading 8 bits.
   *
   * @param value the value to mask
   * @return the masked value
   */
  public static int maskToValue(int value) {
    return maskToWordOfSize(VALUE_LENGTH, value);
  }

  /**
   * Extracts the opcode from a combined integer.
   *
   * @param value the value, 24 bits wide
   * @return the extracted opcode
   */
  public static int extractOpcode(int value) {
    return (value & 0x00f00000) >>> 20;
  }

  /**
   * Extracts the large opcode (8 bit) from a combined integer.
   *
   * This is used for instructions that do not take an argument to be able to encode more than 16
   * different instructions.
   *
   * @param value the value, 24 bits wide
   * @return the extracted opcode
   */
  public static int extractLargeOpcode(int value) {
    return (value & 0x00ff0000) >>> 16;
  }

  /**
   * Extracts the value from a combined integer.
   *
   * @param value the positive value, 20 bits wide (first 4 are reserved for opcode)
   * @return the extracted value
   * @throws NumberOverflowException if the number was more than 24 bits wide
   */
  public static int extractArgument(int value) throws NumberOverflowException {
    return coerceToAddress(value & 0x000fffff);
  }

  /**
   * Extracts the value from a combined integer, where the opcode takes 8 bit.
   *
   * This is used for instructions that do not take an argument to be able to encode more than 16
   * different instructions.
   *
   * @param value the positive value, 20 bits wide (first 4 are reserved for opcode)
   * @return the extracted value
   * @throws NumberOverflowException if the number was more than 24 bits wide
   */
  public static int extractArgumentLargeOpcode(int value) throws NumberOverflowException {
    return coerceToAddress(value & 0x0000ffff);
  }

  /**
   * Combines the given opcode and its argument to a single integer that can be stored in the
   * memory.
   *
   * @param opcode the opcode
   * @param large whether the opcode is 8 bit (large) or not
   * @param argument the argument for it
   * @return the combined instruction
   */
  private static int combineInstruction(int opcode, boolean large, int argument) {
    int result = large ? opcode << 16 : opcode << 20;
    result = result | maskToWordOfSize(large ? 16 : 20, argument);

    return result;
  }

  /**
   * Masks off the first {@code Integer.SIZE - bitLength} bits, to only leave the
   * <strong>lower</strong> {@code bitLength} bits.
   *
   * @param bitLength the target bit length
   * @param value the value
   * @return the value
   */
  private static int maskToWordOfSize(int bitLength, int value) {
    int mask = ~0 >>> (Integer.SIZE - bitLength);
    return value & mask;
  }

  /**
   * Combines the given instruction in a single integer that can be stored in the memory.
   *
   * @param call the instruction call to combine to an int
   * @return the combined instruction
   */
  public static int combineInstruction(InstructionCall call) {
    return combineInstruction(
        call.command().opcode(),
        call.command().opcode() > 0xF,
        call.argument()
    );
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
      int firstOne = output.indexOf('1');
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
