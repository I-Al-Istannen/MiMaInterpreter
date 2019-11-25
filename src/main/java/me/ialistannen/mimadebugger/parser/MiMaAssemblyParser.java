package me.ialistannen.mimadebugger.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.machine.MiMaRegister;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.ast.AssemblerDirectiveLit;
import me.ialistannen.mimadebugger.parser.ast.AssemblerDirectiveOrigin;
import me.ialistannen.mimadebugger.parser.ast.AssemblerDirectiveRegister;
import me.ialistannen.mimadebugger.parser.ast.CommentNode;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelDeclarationNode;
import me.ialistannen.mimadebugger.parser.ast.LabelUsageNode;
import me.ialistannen.mimadebugger.parser.ast.LiteralNode;
import me.ialistannen.mimadebugger.parser.ast.RootNode;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.ast.UnparsableNode;
import me.ialistannen.mimadebugger.parser.processing.ConstantVerification;
import me.ialistannen.mimadebugger.parser.processing.InstructionCallResolver;
import me.ialistannen.mimadebugger.parser.processing.LabelResolver;
import me.ialistannen.mimadebugger.parser.processing.ToMemoryValueConverter;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import me.ialistannen.mimadebugger.parser.validation.ImmutableParsingProblem;
import me.ialistannen.mimadebugger.parser.validation.ParsingProblem;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;
import org.reactfx.util.Either;

/**
 * A parser for MiMa Assembly, supporting labels and comments.
 */
public class MiMaAssemblyParser {

  private static final Pattern COMMENT_PATTERN = Pattern.compile(";");
  private static final Pattern LABEL_JUMP_PATTERN = Pattern.compile("[a-zA-Z][_fa-zA-Z0-9-]*");
  private static final Pattern LABEL_DECLARATION_PATTERN = Pattern.compile(
      LABEL_JUMP_PATTERN.pattern() + "(?=:)"
  );
  private static final Pattern INSTRUCTION_PATTERN = Pattern.compile("[A-Za-z]+");
  private static final Pattern VALUE_PATTERN = Pattern.compile("[+\\-]?(0x)?\\d+");
  private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\n");
  private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");
  private static final Pattern ASSEMBLER_DIRECTIVE = Pattern.compile("\\.[^\n]*");

  private MutableStringReader reader;
  private int address;
  private LabelResolver labelResolver;
  private ConstantVerification constantVerification;
  private InstructionCallResolver instructionCallResolver;
  private ToMemoryValueConverter toMemoryValueConverter;
  private InstructionSet instructionSet;

  /**
   * Creates a new parser using the given {@link InstructionSet}.
   *
   * @param instructionSet the instruction set to use
   */
  public MiMaAssemblyParser(InstructionSet instructionSet) {
    this.instructionSet = instructionSet;

    this.labelResolver = new LabelResolver();
    this.constantVerification = new ConstantVerification(instructionSet);
    this.instructionCallResolver = new InstructionCallResolver();
    this.toMemoryValueConverter = new ToMemoryValueConverter();
  }

  /**
   * Parses the program to a syntax tree.
   *
   * @param program the program text
   * @return the root of the parsed syntax tree
   */
  private SyntaxTreeNode parseProgramToTree(String program) {
    this.reader = new MutableStringReader(program);
    this.address = 0;

    List<SyntaxTreeNode> syntaxTreeNodes = new ArrayList<>();

    while (reader.canRead()) {
      try {
        SyntaxTreeNode syntaxTreeNode = readLine();

        if (syntaxTreeNode != null) {
          syntaxTreeNodes.add(syntaxTreeNode);
        }
      } catch (UnexpectedParseError unexpectedParseError) {
        syntaxTreeNodes.add(new UnparsableNode(
            address, reader, unexpectedParseError.getSpan(), unexpectedParseError.getMessage()
        ));
        readToSavepoint();
      }
    }

    return new RootNode(syntaxTreeNodes, reader.copy());
  }

  /**
   * Parses the program to a syntax tree.
   *
   * @param program the program text
   * @return the root of the validated syntax tree
   */
  public SyntaxTreeNode parseProgramToValidatedTree(String program) {
    SyntaxTreeNode tree = parseProgramToTree(program);

    labelResolver.resolve(tree);
    constantVerification.validateConstants(tree);
    instructionCallResolver.resolveInstructions(tree, instructionSet);

    return tree;
  }

  /**
   * Parses the program to a list of {@link MemoryValue}s.
   *
   * @param program the program text
   * @return the parsed memory values
   */
  public Either<List<ParsingProblem>, List<MemoryValue>> parseProgramToMemoryValues(
      String program) {
    SyntaxTreeNode rootNode = parseProgramToValidatedTree(program);

    Optional<List<MemoryValue>> values = toMemoryValueConverter.process(rootNode);

    //noinspection OptionalIsPresent
    if (values.isPresent()) {
      return Either.right(values.get());
    }

    return Either.left(rootNode.getAllParsingProblems());
  }

  /**
   * Reads a single line of input from the file, which should equal one instruction.
   *
   * @return the read value or null if none
   * @throws UnexpectedParseError if a value could not be read after peeking its start
   */
  private SyntaxTreeNode readLine() throws UnexpectedParseError {
    eatWhitespace();

    if (!reader.canRead()) {
      return null;
    }

    int start = reader.getCursor();

    SyntaxTreeNode node;
    if (reader.peek(COMMENT_PATTERN)) {
      node = readComment();
    } else if (reader.peek(ASSEMBLER_DIRECTIVE)) {
      node = readAssemblerDirective();
    } else if (reader.peek(LABEL_DECLARATION_PATTERN)) {
      LabelDeclarationNode labelNode = readLabelDeclaration();
      SyntaxTreeNode instructionOrValue = readInstructionOrValue();

      if (instructionOrValue != null) {
        labelNode.addChild(instructionOrValue);
        address++;
      }

      node = labelNode;
    } else if (reader.peek(INSTRUCTION_PATTERN)) {
      node = readInstruction();
      address++;
    } else if (reader.peek(VALUE_PATTERN)) {
      node = readValue();
      address++;
    } else if (reader.peek(NEW_LINE_PATTERN)) {
      node = null;
    } else if (reader.peek(WHITE_SPACE)) {
      reader.read(WHITE_SPACE);
      node = null;
    } else {
      int end = reader.getCursor() == start
          ? Math.min(reader.getString().length(), start + 5)
          : reader.getCursor();

      readToSavepoint();
      return new UnparsableNode(
          address, reader, new HalfOpenIntRange(start, end),
          "Expected comment, label or instruction"
      );
    }

    eatWhitespace();

    return node;
  }

  private void readToSavepoint() {
    reader.read(Pattern.compile("[^\\n]+"));
  }

  private SyntaxTreeNode readAssemblerDirective() throws UnexpectedParseError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    assertRead(Pattern.compile("\\."));
    String directive = assertRead(Pattern.compile("\\w+"));
    eatWhitespaceNoNewline();

    int afterDirective = reader.getCursor();

    switch (directive) {
      case "org": {
        ConstantNode value;
        value = readValue();
        address = value.getValue();
        return new AssemblerDirectiveOrigin(
            value, address, reader, new HalfOpenIntRange(start, afterDirective)
        );
      }
      case "lit":
        ConstantNode value = readValue();
        int myAddress = address;
        address++;
        return new AssemblerDirectiveLit(
            myAddress, reader, new HalfOpenIntRange(start, afterDirective), value
        );
      case "reg":
        return readRegisterDirective(start);
      default:
        throw new UnexpectedParseError(
            "Invalid assembly directive '" + directive + "'",
            new HalfOpenIntRange(start, reader.getCursor())
        );
    }
  }

  private SyntaxTreeNode readRegisterDirective(int start) throws UnexpectedParseError {
    int directiveNameEnd = reader.getCursor();
    String registerName = reader.read(Pattern.compile("[A-Za-z]+"));
    MiMaRegister register = null;
    for (MiMaRegister miMaRegister : MiMaRegister.values()) {
      if (miMaRegister.getAbbreviation().equalsIgnoreCase(registerName)) {
        register = miMaRegister;
        break;
      }
    }

    int registerNameEnd = reader.getCursor();

    if (register == null) {
      SyntaxTreeNode node = AssemblerDirectiveRegister.of(
          address, reader, new HalfOpenIntRange(start, registerNameEnd), null, null
      );
      node.addProblem(ImmutableParsingProblem.builder()
          .approximateSpan(new HalfOpenIntRange(directiveNameEnd, registerNameEnd))
          .message("Invalid register")
          .build()
      );
      readToSavepoint();
      return node;
    }

    eatWhitespaceNoNewline();

    if (reader.peek(VALUE_PATTERN)) {
      return AssemblerDirectiveRegister.of(
          address, reader, new HalfOpenIntRange(start, registerNameEnd), register, readValue()
      );
    } else if (reader.peek(LABEL_JUMP_PATTERN)) {
      return AssemblerDirectiveRegister.of(
          address, reader, new HalfOpenIntRange(start, registerNameEnd), register, readLabelUsage()
      );
    } else {
      SyntaxTreeNode node = AssemblerDirectiveRegister.of(
          address, reader, new HalfOpenIntRange(start, registerNameEnd), null, null
      );
      int errorHighlightStart =
          registerNameEnd == reader.getCursor() ? directiveNameEnd : registerNameEnd;
      readToSavepoint();
      node.addProblem(ImmutableParsingProblem.builder()
          .approximateSpan(new HalfOpenIntRange(errorHighlightStart, reader.getCursor()))
          .message("Invalid value")
          .build()
      );
      return node;
    }
  }

  private CommentNode readComment() throws UnexpectedParseError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    assertRead(COMMENT_PATTERN);
    eatWhitespaceNoNewline();
    String readCommentText = reader.read(Pattern.compile("[^\\n]*"));

    return new CommentNode(
        address,
        reader,
        new HalfOpenIntRange(start, reader.getCursor()),
        readCommentText
    );
  }

  /**
   * Reads a label declaration, so sth like {@code label:}.
   *
   * @return the read label declaration
   * @throws UnexpectedParseError if no name could be read
   */
  private LabelDeclarationNode readLabelDeclaration() throws UnexpectedParseError {
    eatWhitespaceNoNewline();
    int startPos = reader.getCursor();
    String name = assertRead(LABEL_DECLARATION_PATTERN);
    reader.read(1); // consume trailing ':'
    return new LabelDeclarationNode(
        name,
        address,
        reader.copy(),
        new HalfOpenIntRange(startPos, reader.getCursor() - 1)
    );
  }

  /**
   * Reads a label usage, so sth like {@code JMP label}.
   *
   * @return the read label
   * @throws UnexpectedParseError if no name could be read
   */
  private LabelUsageNode readLabelUsage() throws UnexpectedParseError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    String name = assertRead(LABEL_JUMP_PATTERN);
    return new LabelUsageNode(
        name,
        address,
        reader.copy(),
        new HalfOpenIntRange(start, reader.getCursor())
    );
  }

  /**
   * Tries to read an instruction or a value.
   *
   * @return the read value or instruction node, null if nothing found
   * @throws UnexpectedParseError if no value or instruction could be read after peeking their
   *     pattern
   */
  private SyntaxTreeNode readInstructionOrValue() throws UnexpectedParseError {
    eatWhitespaceNoNewline();
    if (reader.peek(VALUE_PATTERN)) {
      return readValue();
    } else if (reader.peek(INSTRUCTION_PATTERN)) {
      return readInstruction();
    } else {
      return null;
    }
  }

  /**
   * Tries to read an integer value.
   *
   * @return the read integer value
   * @throws UnexpectedParseError if no value could be read
   */
  private ConstantNode readValue() throws UnexpectedParseError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    String number = assertRead(VALUE_PATTERN);

    if (!reader.peek(Pattern.compile("\\s|$"))) {
      return constantNodeWithProblem(start, "Expected a space character after a value");
    }

    try {
      int value = parseInt(number);
      return new ConstantNode(
          value,
          address,
          reader.copy(),
          new HalfOpenIntRange(start, reader.getCursor())
      );
    } catch (NumberFormatException e) {
      return constantNodeWithProblem(start, "Expected integer number");
    }
  }

  private int parseInt(String number) {
    int value;
    if (number.contains("0x")) {
      value = Integer.parseInt(number.replace("0x", ""), 16);
    } else {
      value = Integer.parseInt(number);
    }
    return value;
  }

  private ConstantNode constantNodeWithProblem(int start, String problem) {
    ConstantNode constantNode = new ConstantNode(
        0,
        address,
        reader.copy(),
        new HalfOpenIntRange(start, reader.getCursor())
    );
    constantNode.addProblem(ImmutableParsingProblem.builder()
        .approximateSpan(constantNode.getSpan())
        .message(problem)
        .build()
    );
    return constantNode;
  }

  /**
   * Tries to read an instruction.
   *
   * @return the read instruction
   * @throws UnexpectedParseError if no instruction was found
   */
  private SyntaxTreeNode readInstruction() throws UnexpectedParseError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    String instructionName = assertRead(INSTRUCTION_PATTERN);

    if (instructionName.equals("LIT")) {
      int end = reader.getCursor();
      ConstantNode value = readValue();
      return new LiteralNode(
          address, reader, new HalfOpenIntRange(start, end), value
      );
    }

    InstructionNode instructionNode = new InstructionNode(
        instructionName,
        address,
        reader.copy(),
        new HalfOpenIntRange(start, reader.getCursor())
    );

    if (!instructionSet.forName(instructionName).isPresent()) {
      instructionNode.addProblem(ImmutableParsingProblem.builder()
          .approximateSpan(instructionNode.getSpan())
          .message("Instruction '" + instructionName + "' not found")
          .build()
      );
      return instructionNode;
    }

    eatWhitespaceNoNewline();

    if (reader.peek(VALUE_PATTERN)) {
      instructionNode.addChild(readValue());
    }

    if (reader.peek(LABEL_JUMP_PATTERN)) {
      instructionNode.addChild(readLabelUsage());
    }

    return instructionNode;
  }

  private String assertRead(Pattern pattern) throws UnexpectedParseError {
    int start = reader.getCursor();
    if (!reader.peek(pattern)) {
      int failingCursorPosition = reader.getCursor();
      if (failingCursorPosition == start) {
        failingCursorPosition = Math.min(start, failingCursorPosition + 5);
      }
      throw new UnexpectedParseError(
          "Expected " + pattern.pattern(),
          new HalfOpenIntRange(start, failingCursorPosition)
      );
    }
    return reader.read(pattern);
  }

  private void eatWhitespace() {
    reader.read(WHITE_SPACE);
  }

  private void eatWhitespaceNoNewline() {
    reader.read(Pattern.compile("[\t ]*"));
  }

  private static class UnexpectedParseError extends Exception {

    private HalfOpenIntRange span;

    UnexpectedParseError(String message, HalfOpenIntRange span) {
      super(message);
      this.span = span;
    }

    HalfOpenIntRange getSpan() {
      return span;
    }
  }
}