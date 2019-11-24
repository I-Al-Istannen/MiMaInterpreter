package me.ialistannen.mimadebugger.gui.text;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.gui.highlighting.DiscontinuousSpans;
import me.ialistannen.mimadebugger.gui.highlighting.HighlightingCategory;
import me.ialistannen.mimadebugger.gui.highlighting.PositionedHighlighting;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.MiMaAssemblyParser;
import me.ialistannen.mimadebugger.parser.ast.AssemblerDirectiveLit;
import me.ialistannen.mimadebugger.parser.ast.AssemblerDirectiveOrigin;
import me.ialistannen.mimadebugger.parser.ast.AssemblerDirectiveRegister;
import me.ialistannen.mimadebugger.parser.ast.CommentNode;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionCallNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelDeclarationNode;
import me.ialistannen.mimadebugger.parser.ast.LabelUsageNode;
import me.ialistannen.mimadebugger.parser.ast.LiteralNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.RootNode;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.ast.UnparsableNode;
import me.ialistannen.mimadebugger.parser.validation.ParsingProblem;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class ProgramTextPane extends BorderPane {

  private final InstructionSet instructionSet;
  private CodeArea codeArea;

  private ObservableSet<Integer> breakpoints;

  private ObjectProperty<SyntaxTreeNode> syntaxTree;

  public ProgramTextPane(InstructionSet instructionSet) {
    this.instructionSet = instructionSet;
    this.codeArea = new CodeArea();
    this.breakpoints = FXCollections.observableSet(new HashSet<>());
    this.syntaxTree = new SimpleObjectProperty<>();
    getStylesheets().add("/css/Highlight.css");

    syntaxTree.addListener((observable, oldValue, newValue) -> updateLineIcons());
    updateLineIcons();

    codeArea.multiPlainChanges()
        .successionEnds(Duration.ofMillis(100))
        .subscribe(ignore -> highlightCode(codeArea.getText()));

    codeArea.setMouseOverTextDelay(Duration.ofMillis(250));
    InstructionHelpPopup.attachTo(codeArea, instructionSet);
    SyntaxErrorPopup.attachTo(codeArea, position -> {
      if (syntaxTree.get() == null) {
        return Optional.empty();
      }
      return syntaxTree.get().getAllParsingProblems().stream()
          .filter(it -> it.approximateSpan().contains(position))
          .findFirst();
    });

    setCenter(codeArea);
  }

  private void updateLineIcons() {
    codeArea.setParagraphGraphicFactory(
        new LineGutterWithBreakpoint(codeArea, breakpoints, syntaxTree, this::breakpointToggled)
    );
  }

  private void highlightCode(String text) {
    SyntaxTreeNode tree = new MiMaAssemblyParser(instructionSet).parseProgramToValidatedTree(text);
    syntaxTree.set(tree);

    computeBasicHighlighting(tree).ifPresent(this::applyBaseHighlighting);

    List<PositionedHighlighting> problems = tree.getAllParsingProblems().stream()
        .map(this::handleSyntaxError)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
    applyErrorHighlighting(problems);
  }

  private void applyBaseHighlighting(StyleSpans<Collection<String>> styles) {
    codeArea.setStyleSpans(0, styles);
  }

  private void applyErrorHighlighting(List<PositionedHighlighting> errorStyles) {
    for (PositionedHighlighting errorStyle : errorStyles) {
      codeArea.setStyleSpans(errorStyle.getStart(), errorStyle.getStyles());
    }
  }

  private Optional<StyleSpans<Collection<String>>> computeBasicHighlighting(SyntaxTreeNode tree) {
    DiscontinuousSpans spans = new DiscontinuousSpans();

    tree.accept(new NodeVisitor() {
      @Override
      public void visit(SyntaxTreeNode node) {
        HighlightingCategory category;
        if (node instanceof ConstantNode) {
          category = HighlightingCategory.VALUE;
        } else if (node instanceof InstructionNode) {
          category = HighlightingCategory.INSTRUCTION;
        } else if (node instanceof LabelUsageNode) {
          category = HighlightingCategory.LABEL_USAGE;
        } else if (node instanceof CommentNode) {
          category = HighlightingCategory.COMMENT;
        } else if (node instanceof LabelDeclarationNode) {
          category = HighlightingCategory.LABEL_DECLARATION;
        } else if (node instanceof LiteralNode) {
          category = HighlightingCategory.INSTRUCTION;
        } else if (node instanceof AssemblerDirectiveRegister) {
          category = HighlightingCategory.INSTRUCTION;
        } else if (node instanceof AssemblerDirectiveLit) {
          category = HighlightingCategory.INSTRUCTION;
        } else if (node instanceof AssemblerDirectiveOrigin) {
          category = HighlightingCategory.INSTRUCTION;
        } else if (node instanceof RootNode) {
          visitChildren(node);
          return;
        } else if (node instanceof InstructionCallNode) {
          visitChildren(node);
          return;
        } else if (node instanceof UnparsableNode) {
          visitChildren(node);
          return;
        } else {
          System.err.println(
              "Invalid highlighting node received: " + node.getClass() + "  " + node
          );
          return;
        }

        spans.addSpan(Collections.singletonList(category.getCssClass()), node.getSpan());

        visitChildren(node);
      }
    });

    if (spans.isEmpty()) {
      int length = tree.getStringReader().getString().length();
      return Optional.of(StyleSpans.singleton(Collections.emptyList(), length));
    }

    return Optional.ofNullable(spans.toStyleSpans());
  }

  private Optional<PositionedHighlighting> handleSyntaxError(ParsingProblem problem) {
    int start = problem.approximateSpan().getStart();
    int end = problem.approximateSpan().getEnd();

    if (end <= start) {
      return Optional.empty();
    }

    StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();

    StyleSpans<Collection<String>> style = builder
        .add(Collections.singletonList("error"), end - start)
        .create();

    return Optional.of(new PositionedHighlighting(style, start));
  }

  private void breakpointToggled(int line) {
    if (breakpoints.contains(line)) {
      breakpoints.remove(line);
    } else {
      breakpoints.add(line);
    }
    updateLineIcons();
  }

  /**
   * Sets the code to display in this pane.
   *
   * @param code the code to display
   */
  public void setCode(String code) {
    codeArea.clear();
    codeArea.appendText(code);
  }

  /**
   * Returns all breakpoints that are currently set.
   *
   * @return all breakpoints that are currently set
   */
  public ObservableSet<Integer> getBreakpoints() {
    return breakpoints;
  }

  /**
   * Returns the code in this pane.
   *
   * @return the code in this pane
   */
  public ObservableValue<String> codeProperty() {
    return codeArea.textProperty();
  }
}
