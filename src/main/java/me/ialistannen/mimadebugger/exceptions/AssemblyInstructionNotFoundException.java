package me.ialistannen.mimadebugger.exceptions;

import me.ialistannen.mimadebugger.parser.util.StringReader;

public class AssemblyInstructionNotFoundException extends MiMaSyntaxError {

  public AssemblyInstructionNotFoundException(String name, int line, StringReader reader) {
    super(String.format("Instruction '%s' not found at line %d!", name, line), reader);
  }
}
