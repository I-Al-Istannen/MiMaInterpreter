package me.ialistannen.mimadebugger.exceptions;

public class InstructionArgumentInvalidFormatException extends MiMaException {

  public InstructionArgumentInvalidFormatException(String instruction, String argument,
      String reason, int line) {
    super(String.format(
        "The argument '%s' was invalid for the instruction '%s' at line %d. Error is: '%s'",
        argument,
        instruction,
        line,
        reason
    ));
  }
}
