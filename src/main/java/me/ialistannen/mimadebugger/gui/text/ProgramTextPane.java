package me.ialistannen.mimadebugger.gui.text;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.gui.highlighting.DiscontinuousSpans;
import me.ialistannen.mimadebugger.gui.highlighting.HighlightingCategory;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.MiMaAssemblyParser;
import me.ialistannen.mimadebugger.parser.ast.CommentNode;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.RootNode;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class ProgramTextPane extends BorderPane {

  private final InstructionSet instructionSet;
  private CodeArea codeArea;

  private ObservableSet<Integer> breakpoints;

  private ObjectProperty<MiMaSyntaxError> error;

  public ProgramTextPane(InstructionSet instructionSet) {
    this.instructionSet = instructionSet;
    this.codeArea = new CodeArea();
    this.breakpoints = FXCollections.observableSet(new HashSet<>());
    this.error = new SimpleObjectProperty<>();
    getStylesheets().add("/css/Highlight.css");

    updateLineIcons();

    // Update the icons when a new line is added to keep the gutter size consistent
    codeArea.getParagraphs()
        .changes()
        .successionEnds(Duration.ofMillis(100))
        .subscribe(changes -> updateLineIcons());

    codeArea.multiPlainChanges()
        .successionEnds(Duration.ofMillis(100))
        .subscribe(ignore -> computeHighlighting(codeArea.getText())
            .ifPresent(styles -> codeArea.setStyleSpans(0, styles))
        );

    codeArea.setMouseOverTextDelay(Duration.ofMillis(250));
    InstructionHelpPopup.attachTo(codeArea, instructionSet);
    SyntaxErrorPopup.attachTo(codeArea, position -> {
      if (error.get() == null) {
        return Optional.empty();
      }
      HalfOpenIntRange span = error.get().getSpan();
      if (span.contains(position)) {
        return Optional.of(error.getValue());
      }
      return Optional.empty();
    });

    setCenter(codeArea);
  }

  private void updateLineIcons() {
    codeArea.setParagraphGraphicFactory(
        new LineGutterWithBreakpoint(codeArea, breakpoints, this::breakpointToggled)
    );
  }

  private Optional<StyleSpans<Collection<String>>> computeHighlighting(String text) {
    try {
      SyntaxTreeNode tree = new MiMaAssemblyParser(instructionSet).parseProgramToTree(text);
      DiscontinuousSpans spans = new DiscontinuousSpans();

      tree.accept(new NodeVisitor() {
        @Override
        public void visit(SyntaxTreeNode node) {
          HighlightingCategory category = HighlightingCategory.NORMAL;
          if (node instanceof ConstantNode) {
            category = HighlightingCategory.VALUE;
          } else if (node instanceof InstructionNode) {
            category = HighlightingCategory.INSTRUCTION;
          } else if (node instanceof LabelNode) {
            if (((LabelNode) node).isDeclaration()) {
              category = HighlightingCategory.LABEL_DECLARATION;
            } else {
              category = HighlightingCategory.LABEL_USAGE;
            }
          } else if (node instanceof CommentNode) {
            category = HighlightingCategory.COMMENT;
          } else if (node instanceof RootNode) {
            return;
          } else {
            System.err.println(
                "Invalid highlighting node received: " + node.getClass() + "  " + node
            );
          }

          spans.addSpan(Collections.singletonList(category.getCssClass()), node.getSpan());
        }
      });

      if (spans.isEmpty()) {
        return Optional.of(StyleSpans.singleton(Collections.emptyList(), text.length()));
      }

      error.set(null);
      return Optional.ofNullable(spans.toStyleSpans());
    } catch (MiMaSyntaxError syntaxError) {
      return handleSyntaxError(syntaxError);
    }
  }

  private Optional<StyleSpans<Collection<String>>> handleSyntaxError(MiMaSyntaxError syntaxError) {
    int start = syntaxError.getSpan().getStart();
    int end = syntaxError.getSpan().getEnd();

    if (end <= start) {
      return Optional.empty();
    }

    StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
    if (start > 0) {
      builder.add(Collections.emptyList(), start);
    }

    error.set(syntaxError);
    return Optional.of(
        builder
            .add(Collections.singletonList("error"), end - start)
            .create()
    );
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
