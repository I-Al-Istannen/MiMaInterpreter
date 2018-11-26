package me.ialistannen.mimadebugger.machine.program;

import static me.ialistannen.mimadebugger.gui.state.EncodedInstructionCall.constantValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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
  void parseInvalidInstruction() {
    List<String> program = Collections.singletonList("HEYHO");
    assertThrows(
        InstructionNotFoundException.class,
        () -> parser.parseFromNames(program)
    );
  }

  @Test
  void parseInvalidNumber() {
    List<String> program = Collections.singletonList("LDC HelloWorld");
    assertThrows(
        InstructionArgumentInvalidFormatException.class,
        () -> parser.parseFromNames(program)
    );
  }

  @Test
  void parseOneLineWithEachInstruction() {
    for (Instruction instruction : instructionSet.getAll()) {
      String argument = instruction.hasArgument() ? "01" : "";
      List<String> program = Collections.singletonList(instruction.name() + " " + argument);

      int argumentValue = instruction.hasArgument() ? 1 : 0;
      assertThat(
          parser.parseFromNames(program),
          is(Collections.singletonList(toValue(instruction, argumentValue, 0)))
      );
    }
  }

  @Test
  void ensureArgumentIsNotIgnoredWhenNotNeeded() {
    for (Instruction instruction : instructionSet.getAll()) {
      if (instruction.hasArgument()) {
        continue;
      }
      int argument = 11;
      List<String> program = Collections.singletonList(instruction.name() + " " + argument);

      MemoryValue expectedResult = toValue(instruction, argument, 0);

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

      List<MemoryValue> parsed = parser.parseFromNames(lines);

      for (int j = 0; j < instructions.size(); j++) {
        assertThat(
            toValue(instructions.get(j), argument, j),
            is(parsed.get(j))
        );
      }
    }
  }

  @Test
  void ensureMaximumValueIsRead() {
    Instruction instruction = getInstructionWithArgument();

    // -1 as 0 counts too
    int argument = 1 << MemoryFormat.ADDRESS_LENGTH - 1;
    List<String> program = Collections.singletonList(instruction.name() + " " + argument);

    MemoryValue expected = toValue(instruction, argument, 0);

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

  @Test
  void ensureBlankLineEqualsZero() {
    List<String> program = Arrays.asList("", "");

    List<MemoryValue> values = parser.parseFromNames(program);

    for (int i = 0; i < values.size(); i++) {
      MemoryValue value = values.get(i);
      assertThat(
          value,
          is(constantValue(0, i))
      );
    }
  }

  @Test
  void ensureValueIsReadCorrectly() {
    List<String> program = Collections.singletonList("12345");

    List<MemoryValue> values = parser.parseFromNames(program);

    assertThat(
        values,
        is(Collections.singletonList(constantValue(12345, 0)))
    );
  }

  @Test
  void ensureNegativeValueIsReadCorrectly() {
    List<String> program = Collections.singletonList("-12345");

    List<MemoryValue> values = parser.parseFromNames(program);

    assertThat(
        values,
        is(Collections.singletonList(constantValue(-12345, 0)))
    );
  }

  @Test
  void ensureTooSmallValueThrowsException() {
    List<String> program = Collections
        .singletonList(Integer.toString(MemoryFormat.VALUE_MINIMUM - 1));

    assertThrows(
        NumberOverflowException.class,
        () -> parser.parseFromNames(program)
    );
  }

  @Test
  void ensureTooLargeValueThrowsException() {
    List<String> program = Collections
        .singletonList(Integer.toString(MemoryFormat.VALUE_MAXIMUM + 1));

    assertThrows(
        NumberOverflowException.class,
        () -> parser.parseFromNames(program)
    );
  }

  @Test
  void ensureMinimumValueCanBeRead() {
    List<String> program = Collections
        .singletonList(Integer.toString(MemoryFormat.VALUE_MINIMUM));

    List<MemoryValue> values = parser.parseFromNames(program);
    assertThat(
        values,
        is(Collections.singletonList(constantValue(MemoryFormat.VALUE_MINIMUM, 0)))
    );
  }

  @Test
  void ensureMaximumValueCanBeRead() {
    List<String> program = Collections
        .singletonList(Integer.toString(MemoryFormat.VALUE_MAXIMUM));

    List<MemoryValue> values = parser.parseFromNames(program);
    assertThat(
        values,
        is(Collections.singletonList(constantValue(MemoryFormat.VALUE_MAXIMUM, 0)))
    );
  }

  private Instruction getInstructionWithArgument() {
    return instructionSet.getAll().stream()
        .filter(Instruction::hasArgument)
        .findFirst()
        .get();
  }

  private MemoryValue toValue(Instruction instruction, int argument, int address) {
    InstructionCall call = toCall(instruction, argument);
    return ImmutableEncodedInstructionCall.builder()
        .instructionCall(call)
        .address(address)
        .representation(MemoryFormat.combineInstruction(call))
        .build();
  }

  private InstructionCall toCall(Instruction instruction, int argument) {
    return ImmutableInstructionCall.builder()
        .command(instruction)
        .argument(argument)
        .build();
  }
}