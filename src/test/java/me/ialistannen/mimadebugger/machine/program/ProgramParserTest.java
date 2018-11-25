package me.ialistannen.mimadebugger.machine.program;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.exceptions.InstructionArgumentInvalidFormatException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall.Builder;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProgramParserTest {

  private ProgramParser parser;
  private InstructionSet instructionSet;

  @BeforeEach
  void setup() {
    instructionSet = new InstructionSet();
    parser = new ProgramParser(instructionSet);
  }

  @Test
  void parseEmptyProgram() {
    List<String> program = Collections.emptyList();
    assertThat(
        parser.parseFromNames(program),
        is(Collections.emptyList())
    );
  }

  @Test
  void parseOneLineWithEachInstruction() {
    for (Instruction instruction : instructionSet.getAll()) {
      String argument = instruction.hasArgument() ? "01" : "";
      List<String> program = Collections.singletonList(instruction.name() + " " + argument);

      Builder expectedResult = ImmutableInstructionCall.builder()
          .command(instruction);

      if (instruction.hasArgument()) {
        expectedResult.argument(1);
      }
      assertThat(
          parser.parseFromNames(program),
          is(Collections.singletonList(expectedResult.build()))
      );
    }
  }

  @Test
  void ensureArgumentIsIgnoredWhenNotNeeded() {
    for (Instruction instruction : instructionSet.getAll()) {
      if (instruction.hasArgument()) {
        continue;
      }
      int argument = 11;
      List<String> program = Collections.singletonList(instruction.name() + " " + argument);

      InstructionCall expectedResult = ImmutableInstructionCall.builder()
          .command(instruction)
          .build();

      assertThat(
          parser.parseFromNames(program),
          is(Collections.singletonList(expectedResult))
      );
    }
  }

  @Test
  void ensureArgumentIsRequiredWhenNeededWithTrailingSpace() {
    for (Instruction instruction : instructionSet.getAll()) {
      if (!instruction.hasArgument()) {
        continue;
      }
      List<String> program = Collections.singletonList(instruction.name() + " ");

      assertThrows(
          InstructionArgumentInvalidFormatException.class,
          () -> parser.parseFromNames(program)
      );
    }
  }

  @Test
  void ensureArgumentIsRequiredWhenNeededWithoutTrailingSpace() {
    for (Instruction instruction : instructionSet.getAll()) {
      if (!instruction.hasArgument()) {
        continue;
      }
      List<String> program = Collections.singletonList(instruction.name());

      assertThrows(
          InstructionArgumentInvalidFormatException.class,
          () -> parser.parseFromNames(program)
      );
    }
  }

  @Test
  void ensureMultipleLinesAreParsed() {
    for (int i = 0; i < 10_000; i++) {
      int argument = 112;

      List<Instruction> instructions = instructionSet.getAll().stream()
          .limit(ThreadLocalRandom.current().nextInt(instructionSet.getAll().size()))
          .collect(Collectors.toList());

      List<String> lines = instructions.stream()
          .map(instruction -> instruction.name() + " " + argument)
          .collect(Collectors.toList());

      List<InstructionCall> parsed = parser.parseFromNames(lines);

      List<InstructionCall> expected = instructions.stream()
          .map(instruction -> toCall(instruction, argument))
          .collect(Collectors.toList());

      assertThat(
          expected,
          is(parsed)
      );
    }
  }

  @Test
  void ensureMaximumValueIsRead() {
    Instruction instruction = getInstructionWithArgument();

    // -1 as 0 counts too
    int argument = 1 << MemoryFormat.ADDRESS_LENGTH - 1;
    List<String> program = Collections.singletonList(instruction.name() + " " + argument);

    InstructionCall expected = toCall(instruction, argument);

    assertThat(
        parser.parseFromNames(program),
        is(Collections.singletonList(expected))
    );
  }

  @Test
  void ensureTooLargeValueIsNotRead() {
    Instruction instruction = getInstructionWithArgument();

    int argument = 1 << MemoryFormat.ADDRESS_LENGTH + 1;
    List<String> program = Collections.singletonList(instruction.name() + " " + argument);

    assertThrows(
        NumberOverflowException.class,
        () -> parser.parseFromNames(program)
    );
  }

  private Instruction getInstructionWithArgument() {
    return instructionSet.getAll().stream()
        .filter(Instruction::hasArgument)
        .findFirst()
        .get();
  }

  private InstructionCall toCall(Instruction instruction, int argument) {
    if (instruction.hasArgument()) {
      return ImmutableInstructionCall.builder()
          .command(instruction)
          .argument(argument)
          .build();
    }

    return ImmutableInstructionCall.builder()
        .command(instruction)
        .build();
  }
}