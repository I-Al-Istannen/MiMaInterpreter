package me.ialistannen.mimadebugger.parser;

import static me.ialistannen.mimadebugger.gui.state.EncodedInstructionCall.constantValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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
import me.ialistannen.mimadebugger.parser.ast.LabelDeclarationNode;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import me.ialistannen.mimadebugger.parser.validation.ParsingProblem;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactfx.util.Either;

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
    assertThat(parseProgramOrThrow("")).isEmpty();
  }

  @Test
  void parseInvalidInstruction() {
    String program = "HEYHO";
    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
    );
  }

  @Test
  void parseInvalidNumberOrLabel() {
    String program = "LDC HelloWorld";
    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
    );
  }

  @Test
  void parseOneLineWithEachInstruction() throws MiMaSyntaxError {
    for (Instruction instruction : instructionSet.getAll()) {
      String argument = instruction.hasArgument() ? "01" : "";
      String program = instruction.name() + " " + argument;

      int argumentValue = instruction.hasArgument() ? 1 : 0;
      assertThat(parseProgramOrThrow(program))
          .containsExactly(toValue(instruction, argumentValue, 0));
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

      assertThat(parseProgramOrThrow(program)).containsExactly(expectedResult);
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
          () -> parseProgramOrThrow(program)
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
          () -> parseProgramOrThrow(program)
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

      List<MemoryValue> parsed = parseProgramOrThrow(lines);

      for (int j = 0; j < instructions.size(); j++) {
        assertThat(toValue(instructions.get(j), argument, j)).isEqualTo(parsed.get(j));
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

    assertThat(parseProgramOrThrow(program)).containsExactly(expected);
  }

  @Test
  void ensureTooLargeValueIsNotRead() {
    Instruction instruction = getInstructionWithArgument();

    int argument = 1 << MemoryFormat.ADDRESS_LENGTH + 1;
    String program = instruction.name() + " " + argument;

    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
    );
  }

  @Test
  void ensureBlankLineIsSkipped() throws MiMaSyntaxError {
    String program = "\n\n";

    List<MemoryValue> values = parseProgramOrThrow(program);

    assertThat(values).isEmpty();
  }

  @Test
  void ensureValueIsReadCorrectly() throws MiMaSyntaxError {
    String program = "12345";

    List<MemoryValue> values = parseProgramOrThrow(program);

    assertThat(values).containsExactly(constantValue(12345, 0));
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private List<MemoryValue> parseProgramOrThrow(String program) throws MiMaSyntaxError {
    Either<List<ParsingProblem>, List<MemoryValue>> result = parser
        .parseProgramToMemoryValues(program);

    if (result.isLeft()) {
      List<ParsingProblem> problems = result.asLeft().get();
      ParsingProblem problem = problems.get(0);
      throw new MiMaSyntaxError(
          problem.message(), new MutableStringReader(program), problem.approximateSpan()
      );
    }

    return result.asRight().get();
  }

  private SyntaxTreeNode parseProgramToTreeOrThrow(String program) throws MiMaSyntaxError {
    SyntaxTreeNode tree = parser.parseProgramToValidatedTree(program);
    if (tree.hasProblem()) {
      ParsingProblem problem = tree.getAllParsingProblems().get(0);
      throw new MiMaSyntaxError(
          problem.message(), tree.getStringReader(), problem.approximateSpan()
      );
    }
    return tree;
  }

  @Test
  void ensureNegativeValueIsReadCorrectly() throws MiMaSyntaxError {
    String program = "-12345";

    List<MemoryValue> values = parseProgramOrThrow(program);

    assertThat(values).containsExactly(constantValue(-12345, 0));
  }

  @Test
  void ensureTooSmallValueThrowsException() {
    String program = Integer.toString(MemoryFormat.VALUE_MINIMUM - 1);

    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
    );
  }

  @Test
  void ensureTooLargeValueThrowsException() {
    String program = Integer.toString(MemoryFormat.VALUE_MAXIMUM + 1);

    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
    );
  }

  @Test
  void ensureMinimumValueCanBeRead() throws MiMaSyntaxError {
    String program = Integer.toString(MemoryFormat.VALUE_MINIMUM);

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(constantValue(MemoryFormat.VALUE_MINIMUM, 0));
  }

  @Test
  void ensureMaximumValueCanBeRead() throws MiMaSyntaxError {
    String program = Integer.toString(MemoryFormat.VALUE_MAXIMUM);

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(constantValue(MemoryFormat.VALUE_MAXIMUM, 0));
  }

  @Test
  void ensureLabelIsFollowedForConstant() throws MiMaSyntaxError {
    String program = "hey: 0\nJMP hey";

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(
        constantValue(0, 0),
        execute(1, 0, Jump.JUMP)
    );
  }

  @Test
  void ensureLabelIsFollowedForInstruction() throws MiMaSyntaxError {
    String program = "hey: LDC 0\nJMP hey";

    List<MemoryValue> values = parseProgramOrThrow(program)
        .stream()
        .sorted(Comparator.comparing(MemoryValue::address))
        .collect(Collectors.toList());
    assertThat(values).containsExactly(
        execute(0, 0, Load.LOAD_CONSTANT),
        execute(1, 0, Jump.JUMP)
    );
  }

  @Test
  void ensureLabelForEmptyLineIsFollowed() throws MiMaSyntaxError {
    String program = "hey:\nJMP hey";

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(execute(0, 0, Jump.JUMP));
  }

  @Test
  void ensureLabelForwardsIsFollowed() throws MiMaSyntaxError {
    String program = "JMP hey\nhey:";

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(execute(0, 1, Jump.JUMP));
  }

  @Test
  void ensureLabelWithHyphensIsAllowed() throws MiMaSyntaxError {
    String program = "hey-there:";

    SyntaxTreeNode tree = parseProgramToTreeOrThrow(program);
    assertThat(
        ((LabelDeclarationNode) tree.getChildren().get(0)).getName()
    ).isEqualTo("hey-there");
  }

  @Test
  void ensureLabelWithHyphensIsNotAllowedAtStart() {
    String program = "-hey-there:";

    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramToTreeOrThrow(program)
    );
  }

  @Test
  void ensureLabelWithNumberIsAllowed() throws MiMaSyntaxError {
    String program = "heyThere2:";

    SyntaxTreeNode tree = parseProgramToTreeOrThrow(program);
    assertThat(((LabelDeclarationNode) tree.getChildren().get(0)).getName()).isEqualTo("heyThere2");
  }

  @Test
  void ensureLabelWithNumberIsNotAllowedAtStart() {
    String program = "2heyThere:";

    assertThrows(
        MiMaSyntaxError.class,
        () -> System.out.println(parseProgramToTreeOrThrow(program))
    );
  }

  @Test
  void ensureLineWithSpaceIsParsed() throws MiMaSyntaxError {
    String program = "  ";

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).isEmpty();
  }

  @Test
  void ensureNegativeLoadConstantIsValid() throws MiMaSyntaxError {
    String program = "LDC -1";

    List<MemoryValue> values = parseProgramOrThrow(program);
    InstructionCall call = ImmutableInstructionCall.builder()
        .argument(-1)
        .command(Load.LOAD_CONSTANT)
        .build();

    assertThat(values.get(0)).isEqualTo(
        ImmutableEncodedInstructionCall.builder()
            .address(0)
            .representation(Load.LOAD_CONSTANT.opcode() | 0x0FFFFF)
            .instructionCall(call)
            .build()
    );
  }

  @Test
  void ensureTooLargeAddressThrowsException() {
    String program = "LDC " + (1 << MemoryFormat.ADDRESS_LENGTH);

    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
    );
  }

  @Test
  void ensureMaximumAddressIsRead() throws MiMaSyntaxError {
    String program = "LDC 1048575";

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(execute(0, 1048575, Load.LOAD_CONSTANT));
  }

  @Test
  void ensureMaximumAddressForTwoBitOpcodeIsRead() throws MiMaSyntaxError {
    String program = "RAR 65535";

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(execute(0, 65535, Other.ROTATE_RIGHT));
  }

  @Test
  void ensureTooLargeAddressForTwoBitOpcodeThrows() {
    String program = "RAR 65536";

    Either<List<ParsingProblem>, List<MemoryValue>> result = parser
        .parseProgramToMemoryValues(program);
    assertThat(result.isLeft()).isTrue();
  }

  @Test
  void ensureReadingInvalidNumberThrowsException() {
    String program = "LDC 12345678910112";

    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
    );
  }

  @Test
  void ensureReadComment() throws MiMaSyntaxError {
    String program = "; this is a comment\nLDC 10";

    List<MemoryValue> values = parseProgramOrThrow(program);
    assertThat(values).containsExactly(execute(0, 10, Load.LOAD_CONSTANT));
  }

  @Test
  void ensureInvalidLine() {
    String program = "äüö";

    assertThrows(
        MiMaSyntaxError.class,
        () -> parseProgramOrThrow(program)
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
