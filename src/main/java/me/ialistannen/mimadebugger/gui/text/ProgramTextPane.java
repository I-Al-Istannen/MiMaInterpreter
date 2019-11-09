package me.ialistannen.mimadebugger.gui.text;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.gui.highlighting.HighlightingCategory;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.MiMaAssemblyParser;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class ProgramTextPane extends BorderPane {

  private CodeArea codeArea;

  private Pattern pattern;

  private ObservableSet<Integer> breakpoints;

  public ProgramTextPane(InstructionSet instructionSet) {
    this.codeArea = new CodeArea();
    this.breakpoints = FXCollections.observableSet(new HashSet<>());
    getStylesheets().add("/css/Highlight.css");

    List<String> instructions = instructionSet.getAll()
        .stream()
        .map(Instruction::name)
        .collect(Collectors.toList());
    Pattern instructionPattern = Pattern.compile("\\b(" + String.join("|", instructions) + ")\\b");
    Pattern argumentPattern = Pattern.compile("\\b\\d{1,8}\\b");
    Pattern binaryValuePattern = Pattern.compile("\\b[0,1]{8,}\\b");
    Pattern commentPattern = Pattern.compile("\\s*//.+");
    Pattern labelDeclarationPattern = Pattern.compile("\\b[a-zA-Z]+(?=:)\\b");
    Pattern labelUsagePattern = Pattern.compile(" \\b[a-zA-Z]+ *(?=(//|\\n|$))");

    pattern = Pattern.compile(
        "(?<INSTRUCTION>" + instructionPattern + ")"
            + "|(?<VALUE>" + argumentPattern + ")"
            + "|(?<BINARY>" + binaryValuePattern + ")"
            + "|(?<COMMENT>" + commentPattern + ")"
            + "|(?<LABELDECLARATION>" + labelDeclarationPattern + ")"
            + "|(?<LABELUSAGE>" + labelUsagePattern + ")"
    );

    updateLineIcons();

    // Update the icons when a new line is added to keep the gutter size consistent
    codeArea.getParagraphs()
        .changes()
        .successionEnds(Duration.ofMillis(100))
        .subscribe(changes -> updateLineIcons());

    codeArea.multiPlainChanges()
        .successionEnds(Duration.ofMillis(100))
        .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));

    codeArea.multiPlainChanges()
        .successionEnds(Duration.ofMillis(200))
        .subscribe(changes -> {
          try {
            new MiMaAssemblyParser(instructionSet).parseProgramToMemoryValues(codeArea.getText());
          } catch (MiMaSyntaxError syntaxError) {
            String text = syntaxError.getReader().getString();
            int end = syntaxError.getReader().getCursor();
            // find start of line. Good way to do this? Just a single char?
            int start;
            for (start = end - 1; start > 0; start--) {
              if (text.charAt(start) == '\n') {
                break;
              }
            }
            codeArea.setStyleSpans(
                start,
                new StyleSpansBuilder<Collection<String>>()
                    .add(Collections.singletonList("error"), end - start)
                    .create()
            );
          }
        });

    codeArea.setMouseOverTextDelay(Duration.ofSeconds(1));
    InstructionHelpPopup.attachTo(codeArea, instructionSet);

    setCenter(codeArea);
  }

  private void updateLineIcons() {
    codeArea.setParagraphGraphicFactory(
        new LineGutterWithBreakpoint(codeArea, breakpoints, this::breakpointToggled)
    );
  }

  private StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = pattern.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

    while (matcher.find()) {
      String styleClass =
          matcher.group("COMMENT") != null
              ? HighlightingCategory.COMMENT.getCssClass()
              : matcher.group("LABELDECLARATION") != null
                  ? HighlightingCategory.LABEL_DECLARATION.getCssClass()
                  : matcher.group("LABELUSAGE") != null
                      ? HighlightingCategory.LABEL_USAGE.getCssClass()
                      : matcher.group("INSTRUCTION") != null
                          ? HighlightingCategory.INSTRUCTION.getCssClass()
                          : matcher.group("BINARY") != null
                              ? HighlightingCategory.BINARY.getCssClass()
                              : matcher.group("VALUE") != null
                                  ? HighlightingCategory.VALUE.getCssClass()
                                  : null; /* never happens */
      assert styleClass != null;

      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
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
