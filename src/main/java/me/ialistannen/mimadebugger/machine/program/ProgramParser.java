package me.ialistannen.mimadebugger.machine.program;

import static me.ialistannen.mimadebugger.gui.state.EncodedInstructionCall.constantValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.mimadebugger.exceptions.InstructionArgumentInvalidFormatException;
import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.gui.state.ImmutableEncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
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
  public List<MemoryValue> parseFromNames(List<String> lines) {
    List<MemoryValue> values = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      MemoryValue memoryValue = parseLine(line, i);

      if (memoryValue != null) {
        values.add(memoryValue);
      }
    }

    return values;
  }

  private MemoryValue parseLine(String line, int lineNumber) {
    if (line.isEmpty()) {
      return null;
    }
    if (line.matches("([+\\-])?\\d+")) {
      return constantValue(ensureInValueRange(Integer.parseInt(line)), lineNumber);
    }

    InstructionCall instructionCall = parseInstructionWithName(line.split("\\s+"), lineNumber);
    return ImmutableEncodedInstructionCall.builder()
        .address(lineNumber)
        .instructionCall(instructionCall)
        .representation(MemoryFormat.combineInstruction(instructionCall))
        .build();
  }

  private int ensureInValueRange(int input) {
    if (input < MemoryFormat.VALUE_MINIMUM || input > MemoryFormat.VALUE_MAXIMUM) {
      throw new NumberOverflowException(input, 24);
    }

    return input;
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
