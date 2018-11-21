package me.ialistannen.mimadebugger.exceptions;

public class ProgramHaltException extends MiMaException {

  public ProgramHaltException() {
    super("Program execution halted normally!");
  }
}
