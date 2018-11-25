package me.ialistannen.mimadebugger.machine.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.exceptions.InstructionArgumentInvalidFormatException;
import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.util.MemoryFormat;

/**
 * Parses a program in String form to a series of {@link InstructionCall}s.
 */
public class ProgramParser {

  private InstructionSet instructionSet;

  /**
   * Creates a new ProgramParser using the given {@link InstructionSet}.
   *
   * @param instructionSet the {@link InstructionSet} to use
   */
  public ProgramParser(InstructionSet instructionSet) {
    this.instructionSet = instructionSet;
  }

  /**
   * Parses a list of lines to a series of {@link InstructionCall}s.
   *
   * The format is:
   * <pre>
   *   NAME ARGUMENT
   * </pre>
   *
   * @param lines the lines to parse
   * @return the parsed calls
   * @throws InstructionNotFoundException if the instructions was not found
   * @throws InstructionArgumentInvalidFormatException if the argument was invalid or not present
   * @throws NumberOverflowException if the argument was too big to fit
   */
  public List<InstructionCall> parseFromNames(List<String> lines) {
    List<InstructionCall> calls = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String[] parts = lines.get(i).trim().split(" ");

      InstructionCall instructionCall = parseInstructionWithName(parts, i);

      calls.add(instructionCall);
    }

    return calls;
  }

  private InstructionCall parseInstructionWithName(String[] nameAndArg, int line) {
    String instructionName = nameAndArg[0];
    Instruction instruction = instructionSet.forName(instructionName)
        .orElseThrow(() -> new InstructionNotFoundException(instructionName, line));

    if (nameAndArg.length != 2 && instruction.hasArgument()) {
      throw new InstructionArgumentInvalidFormatException(
          Arrays.toString(nameAndArg), "<empty>", "Needs an argument", line
      );
    }

    if (nameAndArg.length == 1) {
      return ImmutableInstructionCall.builder()
          .command(instruction)
          .build();
    }

    int argument;
    try {
      argument = Integer.parseInt(nameAndArg[1]);
    } catch (NumberFormatException e) {
      throw new InstructionArgumentInvalidFormatException(
          instructionName,
          nameAndArg[1],
          e.getMessage(),
          line
      );
    }

    return ImmutableInstructionCall.builder()
        .argument(MemoryFormat.coerceToAddress(argument))
        .command(instruction)
        .build();
  }
}
