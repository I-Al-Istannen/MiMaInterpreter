package me.ialistannen.mimadebugger.exceptions;

import me.ialistannen.mimadebugger.parser.util.StringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

public class AssemblyInstructionNotFoundException extends MiMaSyntaxError {

  public AssemblyInstructionNotFoundException(String name, StringReader reader,
      HalfOpenIntRange span) {
    super(
        String.format(
            "Instruction '%s' not found at [(]%d-%d)!",
            name, span.getStart(), span.getEnd()
        ),
        reader,
        span)
    ;
  }
}
