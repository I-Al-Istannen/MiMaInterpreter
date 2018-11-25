package me.ialistannen.mimadebugger.exceptions;

public class NumberOverflowException extends MiMaException {

  public NumberOverflowException(int number, int length) {
    super(
        String.format("The number %d of length %d overflowed or is negative!", number, length)
    );
  }
}
