package me.ialistannen.mimadebugger.machine.program;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.exceptions.InstructionArgumentInvalidFormatException;
import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;

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
   */
  public List<InstructionCall> parseFromNames(List<String> lines) {
    return lines.stream()
        .map(String::trim)
        .map(s -> s.split(" "))
        .map(this::parseInstructionWithName)
        .collect(toList());
  }

  private InstructionCall parseInstructionWithName(String[] nameAndArg) {
    if (nameAndArg.length < 1) {
      throw new InstructionArgumentInvalidFormatException(
          Arrays.toString(nameAndArg), "Not enough arguments, array is empty"
      );
    }

    String instructionName = nameAndArg[0];
    Instruction instruction = instructionSet.forName(instructionName)
        .orElseThrow(() -> new InstructionNotFoundException(instructionName));

    if (!instruction.hasArgument()) {
      return ImmutableInstructionCall.builder()
          .command(instruction)
          .build();
    }

    if (nameAndArg.length != 2) {
      throw new InstructionArgumentInvalidFormatException(
          Arrays.toString(nameAndArg), "Not enough arguments, needed 2"
      );
    }

    int argument;
    try {
      argument = Integer.parseInt(nameAndArg[1]);
    } catch (NumberFormatException e) {
      throw new InstructionArgumentInvalidFormatException(instructionName, nameAndArg[1]);
    }
    return ImmutableInstructionCall.builder()
        .argument(argument)
        .command(instruction)
        .build();
  }
}
