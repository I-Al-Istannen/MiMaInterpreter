package me.ialistannen.mimadebugger.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import me.ialistannen.mimadebugger.exceptions.AssemblyInstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.ast.CommentNode;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelNode;
import me.ialistannen.mimadebugger.parser.ast.RootNode;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.processing.ConstantVerification;
import me.ialistannen.mimadebugger.parser.processing.InstructionCallResolver;
import me.ialistannen.mimadebugger.parser.processing.LabelResolver;
import me.ialistannen.mimadebugger.parser.processing.ToMemoryValueConverter;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;

/**
 * A parser for MiMa Assembly, supporting labels and comments.
 */
public class MiMaAssemblyParser {

  private static final Pattern COMMENT_PATTERN = Pattern.compile(";");
  private static final Pattern LABEL_DECLARATION_PATTERN = Pattern.compile("[a-zA-Z]+(?=:)");
  private static final Pattern LABEL_JUMP_PATTERN = Pattern.compile("[a-zA-Z]+");
  private static final Pattern INSTRUCTION_PATTERN = Pattern.compile("[A-Za-z]{1,5}");
  private static final Pattern VALUE_PATTERN = Pattern.compile("[+\\-]?\\d+");
  private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\n");
  private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

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
    this.constantVerification = new ConstantVerification();
    this.instructionCallResolver = new InstructionCallResolver();
    this.toMemoryValueConverter = new ToMemoryValueConverter();
  }

  /**
   * Parses the program to a syntax tree.
   *
   * @param program the program text
   * @return the root of the parsed syntax tree
   */
  public SyntaxTreeNode parseProgramToTree(String program) throws MiMaSyntaxError {
    this.reader = new MutableStringReader(program);
    this.address = 0;

    List<SyntaxTreeNode> syntaxTreeNodes = new ArrayList<>();

    while (reader.canRead()) {
      SyntaxTreeNode syntaxTreeNode = readLine();

      if (syntaxTreeNode != null) {
        syntaxTreeNodes.add(syntaxTreeNode);
      }
    }

    return new RootNode(syntaxTreeNodes, reader.copy());
  }

  /**
   * Parses the program to a syntax tree.
   *
   * @param program the program text
   * @return the root of the validated syntax tree
   * @throws MiMaSyntaxError if there is an error
   */
  public SyntaxTreeNode parseProgramToValidatedTree(String program) throws MiMaSyntaxError {
    SyntaxTreeNode tree = parseProgramToTree(program);

    // Fail if deeper constant validation fails
    parseProgramToMemoryValues(program);

    return tree;
  }

  /**
   * Parses the program to a list of {@link MemoryValue}s.
   *
   * @param program the program text
   * @return the parsed memory values
   * @throws MiMaSyntaxError if the program has a syntax error
   */
  public List<MemoryValue> parseProgramToMemoryValues(String program) throws MiMaSyntaxError {
    SyntaxTreeNode rootNode = parseProgramToTree(program);
    labelResolver.resolve(rootNode);
    constantVerification.validateConstants(rootNode);
    instructionCallResolver.resolveInstructions(rootNode, instructionSet);

    return toMemoryValueConverter.process(rootNode);
  }

  /**
   * Reads a single line of input from the file, which should equal one instruction.
   *
   * @return the read value or null if none
   */
  private SyntaxTreeNode readLine() throws MiMaSyntaxError {
    eatWhitespace();

    if (!reader.canRead()) {
      return null;
    }

    int start = reader.getCursor();

    SyntaxTreeNode node;
    if (reader.peek(COMMENT_PATTERN)) {
      node = readComment();
    } else if (reader.peek(LABEL_DECLARATION_PATTERN)) {
      LabelNode labelNode = readLabelDeclaration();
      SyntaxTreeNode instructionOrValue = readInstructionOrValue();

      if (instructionOrValue != null) {
        labelNode.addChild(instructionOrValue);
      }

      address++;

      node = labelNode;
    } else if (reader.peek(INSTRUCTION_PATTERN)) {
      node = readInstruction();
      address++;
    } else if (reader.peek(VALUE_PATTERN)) {
      node = readValue();
      address++;
    } else if (reader.peek(NEW_LINE_PATTERN)) {
      node = null;
      address++;
    } else if (reader.peek(WHITE_SPACE)) {
      reader.read(WHITE_SPACE);
      node = null;
    } else {
      int end = reader.getCursor() == start
          ? Math.min(reader.getString().length(), start + 5)
          : reader.getCursor();
      throw new MiMaSyntaxError(
          "Expected comment, label or instruction",
          reader,
          new HalfOpenIntRange(start, end)
      );
    }

    eatWhitespace();

    return node;
  }

  private CommentNode readComment() throws MiMaSyntaxError {
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
   */
  private LabelNode readLabelDeclaration() throws MiMaSyntaxError {
    eatWhitespaceNoNewline();
    int startPos = reader.getCursor();
    String name = assertRead(LABEL_DECLARATION_PATTERN);
    reader.read(1); // consume trailing ':'
    return new LabelNode(
        name,
        true,
        address,
        reader.copy(),
        new HalfOpenIntRange(startPos, reader.getCursor() - 1)
    );
  }

  /**
   * Reads a label usage, so sth like {@code JMP label}.
   *
   * @return the read label
   */
  private LabelNode readLabelUsage() throws MiMaSyntaxError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    String name = assertRead(LABEL_JUMP_PATTERN);
    return new LabelNode(
        name,
        false,
        address,
        reader.copy(),
        new HalfOpenIntRange(start, reader.getCursor())
    );
  }

  /**
   * Tries to read an instruction or a value.
   *
   * @return the read value or instruction node, null if nothing found
   */
  private SyntaxTreeNode readInstructionOrValue() throws MiMaSyntaxError {
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
   * @throws MiMaSyntaxError if the value is no integer
   */
  private SyntaxTreeNode readValue() throws MiMaSyntaxError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    String number = assertRead(VALUE_PATTERN);

    try {
      return new ConstantNode(
          Integer.parseInt(number),
          address,
          reader.copy(),
          new HalfOpenIntRange(start, reader.getCursor())
      );
    } catch (NumberFormatException e) {
      int endPosition = reader.getCursor();
      reader.setCursor(start);
      throw new MiMaSyntaxError(
          "Expected integer number", reader, new HalfOpenIntRange(start, endPosition));
    }
  }

  /**
   * Tries to read an instruction.
   *
   * @return the read instruction
   * @throws MiMaSyntaxError if no instruction was found
   */
  private SyntaxTreeNode readInstruction() throws MiMaSyntaxError {
    eatWhitespaceNoNewline();
    int start = reader.getCursor();
    String instructionName = assertRead(INSTRUCTION_PATTERN);

    if (!instructionSet.forName(instructionName).isPresent()) {
      int failingCursorPosition = reader.getCursor();
      reader.setCursor(start);
      throw new AssemblyInstructionNotFoundException(
          instructionName,
          reader,
          new HalfOpenIntRange(start, failingCursorPosition)
      );
    }

    InstructionNode instructionNode = new InstructionNode(
        instructionName,
        address,
        reader.copy(),
        new HalfOpenIntRange(start, reader.getCursor())
    );

    eatWhitespace();

    if (reader.peek(VALUE_PATTERN)) {
      instructionNode.addChild(readValue());
    }

    if (reader.peek(LABEL_JUMP_PATTERN)) {
      instructionNode.addChild(readLabelUsage());
    }

    return instructionNode;
  }

  private String assertRead(Pattern pattern) throws MiMaSyntaxError {
    int start = reader.getCursor();
    if (!reader.peek(pattern)) {
      int failingCursorPosition = reader.getCursor();
      if (failingCursorPosition == start) {
        failingCursorPosition = Math.min(start, failingCursorPosition + 5);
      }
      throw new MiMaSyntaxError(
          "Expected " + pattern.pattern(),
          reader,
          new HalfOpenIntRange(start, failingCursorPosition)
      );
    }
    return reader.read(pattern);
  }

  private void eatWhitespace() {
    reader.read(WHITE_SPACE);
  }

  private void eatWhitespaceNoNewline() {
    reader.read(Pattern.compile("[\t ]"));
  }

}