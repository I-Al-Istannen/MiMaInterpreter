package me.ialistannen.mimadebugger.exceptions;

public class InstructionArgumentInvalidFormatException extends MiMaException {

  public InstructionArgumentInvalidFormatException(String instruction, String argument) {
    super(String.format(
        "The argument '%s' was invalid for the instruction '%s'", argument, instruction
    ));
  }
}
