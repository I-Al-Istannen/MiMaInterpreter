package me.ialistannen.mimadebugger.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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
import me.ialistannen.mimadebugger.util.ClosedIntRange;

/**
 * A parser for MiMa Assembly, supporting labels and comments.
 */
public class MiMaAssemblyParser {

  private static final Pattern COMMENT_PATTERN = Pattern.compile("//");
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
      throw new MiMaSyntaxError(
          "Expected comment, label or instruction", reader
      );
    }

    eatWhitespace();

    return node;
  }

  private CommentNode readComment() throws MiMaSyntaxError {
    int start = reader.getCursor();
    assertRead(COMMENT_PATTERN);
    eatWhitespaceNoNewline();
    String readCommentText = reader.read(Pattern.compile("[^\\n]*"));

    return new CommentNode(
        address,
        reader,
        new ClosedIntRange(start, reader.getCursor() - 1),
        readCommentText
    );
  }

  /**
   * Reads a label declaration, so sth like {@code label:}.
   *
   * @return the read label declaration
   */
  private LabelNode readLabelDeclaration() throws MiMaSyntaxError {
    int startPos = reader.getCursor();
    String name = assertRead(LABEL_DECLARATION_PATTERN);
    reader.read(1); // consume trailing ':'
    return new LabelNode(
        name,
        true,
        address,
        reader.copy(),
        new ClosedIntRange(startPos, reader.getCursor() - 2)
    );
  }

  /**
   * Reads a label usage, so sth like {@code JMP label}.
   *
   * @return the read label
   */
  private LabelNode readLabelUsage() throws MiMaSyntaxError {
    int start = reader.getCursor();
    String name = assertRead(LABEL_JUMP_PATTERN);
    return new LabelNode(
        name,
        false,
        address,
        reader.copy(),
        new ClosedIntRange(start, reader.getCursor() - 1)
    );
  }

  /**
   * Tries to read an instruction or a value.
   *
   * @return the read value or instruction node, null if nothing found
   */
  private SyntaxTreeNode readInstructionOrValue() throws MiMaSyntaxError {
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
    int start = reader.getCursor();
    String number = assertRead(VALUE_PATTERN);

    try {
      return new ConstantNode(
          Integer.parseInt(number),
          address,
          reader.copy(),
          new ClosedIntRange(start, reader.getCursor() - 1)
      );
    } catch (NumberFormatException e) {
      throw new MiMaSyntaxError(
          "Expected integer number", reader
      );
    }
  }

  /**
   * Tries to read an instruction.
   *
   * @return the read instruction
   * @throws MiMaSyntaxError if no instruction was found
   */
  private SyntaxTreeNode readInstruction() throws MiMaSyntaxError {
    int start = reader.getCursor();
    String instructionName = assertRead(INSTRUCTION_PATTERN);

    if (!instructionSet.forName(instructionName).isPresent()) {
      throw new MiMaSyntaxError("Unknown instruction: '" + instructionName + "'", reader);
    }

    InstructionNode instructionNode = new InstructionNode(
        instructionName,
        address,
        reader.copy(),
        new ClosedIntRange(start, reader.getCursor() - 1)
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
    if (!reader.peek(pattern)) {
      throw new MiMaSyntaxError("Expected " + pattern.pattern(), reader);
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