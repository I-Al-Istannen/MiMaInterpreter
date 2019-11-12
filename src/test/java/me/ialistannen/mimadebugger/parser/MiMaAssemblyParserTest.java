package me.ialistannen.mimadebugger.parser;

import static me.ialistannen.mimadebugger.gui.state.EncodedInstructionCall.constantValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.exceptions.AssemblyInstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.gui.state.EncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.ImmutableEncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Jump;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Load;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Other;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiMaAssemblyParserTest {

  private MiMaAssemblyParser parser;
  private InstructionSet instructionSet;

  @BeforeEach
  void setup() {
    instructionSet = new InstructionSet();
    parser = new MiMaAssemblyParser(instructionSet);
  }

  @Test
  void parseEmptyProgram() throws MiMaSyntaxError {
    assertThat(
        parser.parseProgramToMemoryValues(""),
        is(Collections.emptyList())
    );
  }

  @Test
  void parseInvalidInstruction() {
    String program = "HEYHO";
    assertThrows(
        AssemblyInstructionNotFoundException.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void parseInvalidNumberOrLabel() {
    String program = "LDC HelloWorld";
    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void parseOneLineWithEachInstruction() throws MiMaSyntaxError {
    for (Instruction instruction : instructionSet.getAll()) {
      String argument = instruction.hasArgument() ? "01" : "";
      String program = instruction.name() + " " + argument;

      int argumentValue = instruction.hasArgument() ? 1 : 0;
      assertThat(
          parser.parseProgramToMemoryValues(program),
          is(Collections.singletonList(toValue(instruction, argumentValue, 0)))
      );
    }
  }

  @Test
  void ensureArgumentIsNotIgnoredWhenNotNeeded() throws MiMaSyntaxError {
    for (Instruction instruction : instructionSet.getAll()) {
      if (instruction.hasArgument()) {
        continue;
      }
      int argument = 11;
      String program = instruction.name() + " " + argument;

      MemoryValue expectedResult = toValue(instruction, argument, 0);

      assertThat(
          parser.parseProgramToMemoryValues(program),
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
      String program = instruction.name() + " ";

      assertThrows(
          MiMaSyntaxError.class,
          () -> parser.parseProgramToMemoryValues(program)
      );
    }
  }

  @Test
  void ensureArgumentIsRequiredWhenNeededWithoutTrailingSpace() {
    for (Instruction instruction : instructionSet.getAll()) {
      if (!instruction.hasArgument()) {
        continue;
      }
      String program = instruction.name();

      assertThrows(
          MiMaSyntaxError.class,
          () -> parser.parseProgramToMemoryValues(program)
      );
    }
  }

  @Test
  void ensureMultipleLinesAreParsed() throws MiMaSyntaxError {
    for (int i = 0; i < 1_000; i++) {
      int argument = 112;

      List<Instruction> instructions = instructionSet.getAll().stream()
          .limit(ThreadLocalRandom.current().nextInt(instructionSet.getAll().size()))
          .collect(Collectors.toList());

      String lines = instructions.stream()
          .map(instruction -> instruction.name() + " " + argument)
          .collect(Collectors.joining(System.lineSeparator()));

      List<MemoryValue> parsed = parser.parseProgramToMemoryValues(lines);

      for (int j = 0; j < instructions.size(); j++) {
        assertThat(
            toValue(instructions.get(j), argument, j),
            is(parsed.get(j))
        );
      }
    }
  }

  @Test
  void ensureMaximumValueIsRead() throws MiMaSyntaxError {
    Instruction instruction = getInstructionWithArgument();

    // -1 as 0 counts too
    int argument = 1 << MemoryFormat.ADDRESS_LENGTH - 1;
    String program = instruction.name() + " " + argument;

    MemoryValue expected = toValue(instruction, argument, 0);

    assertThat(
        parser.parseProgramToMemoryValues(program),
        is(Collections.singletonList(expected))
    );
  }

  @Test
  void ensureTooLargeValueIsNotRead() {
    Instruction instruction = getInstructionWithArgument();

    int argument = 1 << MemoryFormat.ADDRESS_LENGTH + 1;
    String program = instruction.name() + " " + argument;

    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void ensureBlankLineIsSkipped() throws MiMaSyntaxError {
    String program = "\n\n";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);

    assertThat(
        values,
        is(Collections.emptyList())
    );
  }

  @Test
  void ensureValueIsReadCorrectly() throws MiMaSyntaxError {
    String program = "12345";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);

    assertThat(
        values,
        is(Collections.singletonList(constantValue(12345, 0)))
    );
  }

  @Test
  void ensureNegativeValueIsReadCorrectly() throws MiMaSyntaxError {
    String program = "-12345";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);

    assertThat(
        values,
        is(Collections.singletonList(constantValue(-12345, 0)))
    );
  }

  @Test
  void ensureTooSmallValueThrowsException() {
    String program = Integer.toString(MemoryFormat.VALUE_MINIMUM - 1);

    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void ensureTooLargeValueThrowsException() {
    String program = Integer.toString(MemoryFormat.VALUE_MAXIMUM + 1);

    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void ensureMinimumValueCanBeRead() throws MiMaSyntaxError {
    String program = Integer.toString(MemoryFormat.VALUE_MINIMUM);

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.singletonList(constantValue(MemoryFormat.VALUE_MINIMUM, 0)))
    );
  }

  @Test
  void ensureMaximumValueCanBeRead() throws MiMaSyntaxError {
    String program = Integer.toString(MemoryFormat.VALUE_MAXIMUM);

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.singletonList(constantValue(MemoryFormat.VALUE_MAXIMUM, 0)))
    );
  }

  @Test
  void ensureLabelIsFollowedForConstant() throws MiMaSyntaxError {
    String program = "hey: 0\nJMP hey";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Arrays.asList(
            constantValue(0, 0),
            execute(1, 0, Jump.JUMP)
        ))
    );
  }

  @Test
  void ensureLabelIsFollowedForInstruction() throws MiMaSyntaxError {
    String program = "hey: LDC 0\nJMP hey";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program)
        .stream()
        .sorted(Comparator.comparing(MemoryValue::address))
        .collect(Collectors.toList());
    assertThat(
        values,
        is(Arrays.asList(
            execute(0, 0, Load.LOAD_CONSTANT),
            execute(1, 0, Jump.JUMP)
        ))
    );
  }

  @Test
  void ensureLabelForEmptyLineIsFollowed() throws MiMaSyntaxError {
    String program = "hey:\nJMP hey";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.singletonList(
            execute(1, 0, Jump.JUMP)
        ))
    );
  }

  @Test
  void ensureLabelForwardsIsFollowed() throws MiMaSyntaxError {
    String program = "JMP hey\nhey:";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.singletonList(
            execute(0, 1, Jump.JUMP)
        ))
    );
  }

  @Test
  void ensureLineWithSpaceIsParsed() throws MiMaSyntaxError {
    String program = "  ";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.emptyList())
    );
  }

  @Test
  void ensureNegativeAddressThrowsException() {
    String program = "LDC -1";

    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void ensureTooLargeAddressThrowsException() {
    String program = "LDC 524289";

    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void ensureMaximumAddressIsRead() throws MiMaSyntaxError {
    String program = "LDC 524288";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.singletonList(
            execute(0, 524288, Load.LOAD_CONSTANT)
        ))
    );
  }

  @Test
  void ensureMaximumAddressForTwoBitOpcodeIsRead() throws MiMaSyntaxError {
    String program = "RAR 32768";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.singletonList(
            execute(0, 32768, Other.ROTATE_RIGHT)
        ))
    );
  }

//  void ensureTooLargeAddressForTwoBitOpcode() {
//    String program = "RAR 32769";
//
//    assertThrows(
//        MiMaSyntaxError.class,
//        () -> parser.parseProgramToMemoryValues(program)
//    );
//  }

  @Test
  void ensureReadingInvalidNumberThrowsException() {
    String program = "LDC 12345678910112";

    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
    );
  }

  @Test
  void ensureReadComment() throws MiMaSyntaxError {
    String program = "; this is a comment\nLDC 10";

    List<MemoryValue> values = parser.parseProgramToMemoryValues(program);
    assertThat(
        values,
        is(Collections.singletonList(
            execute(0, 10, Load.LOAD_CONSTANT)
        ))
    );
  }

  @Test
  void ensureInvalidLine() {
    String program = "äüö";

    assertThrows(
        MiMaSyntaxError.class,
        () -> parser.parseProgramToMemoryValues(program)
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

  private EncodedInstructionCall execute(int address, int value, Instruction instruction) {
    ImmutableInstructionCall instructionCall = ImmutableInstructionCall.builder()
        .command(instruction)
        .argument(value)
        .build();
    return ImmutableEncodedInstructionCall.builder()
        .address(address)
        .representation(MemoryFormat.combineInstruction(instructionCall))
        .instructionCall(instructionCall)
        .build();
  }
}