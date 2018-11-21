package me.ialistannen.mimadebugger.exceptions;

public class MiMaException extends RuntimeException {

  public MiMaException(String message) {
    super(message);
  }

  public MiMaException(String message, Throwable cause) {
    super(message, cause);
  }
}
