package me.ialistannen.mimadebugger.exceptions;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

public class AssemblyInstructionNotFoundException extends MiMaSyntaxError {

  public AssemblyInstructionNotFoundException(String name, int line, StringReader reader,
      HalfOpenIntRange span) {
    super(String.format("Instruction '%s' not found at line %d!", name, line), reader, span);
  }
}
