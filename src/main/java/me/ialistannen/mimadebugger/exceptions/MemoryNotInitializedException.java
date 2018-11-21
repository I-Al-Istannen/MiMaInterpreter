package me.ialistannen.mimadebugger.exceptions;

public class MemoryNotInitializedException extends MiMaException {

  public MemoryNotInitializedException(int address) {
    super(String.format("The memory at the position %d was not set!", address));
  }
}
