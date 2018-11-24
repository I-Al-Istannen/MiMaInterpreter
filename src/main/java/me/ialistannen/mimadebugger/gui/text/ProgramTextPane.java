package me.ialistannen.mimadebugger.gui.text;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class ProgramTextPane extends BorderPane {

  private CodeArea codeArea;

  private Pattern pattern;

  public ProgramTextPane(Collection<String> instructions) {
    this.codeArea = new CodeArea();
    getStylesheets().add("/css/Highlight.css");

    Pattern insructionPattern = Pattern.compile("\\b" + String.join("|", instructions) + "\\b");
    Pattern argumentPattern = Pattern.compile("\\b\\d{1,8}\\b");
    Pattern binaryValuePattern = Pattern.compile("\\b[0,1]{8,}\\b");

    pattern = Pattern.compile(
        "(?<INSTRUCTION>" + insructionPattern + ")"
            + "|(?<ARGUMENT>" + argumentPattern + ")"
            + "|(?<BINARY>" + binaryValuePattern + ")"
    );

    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

    codeArea.multiPlainChanges()
        .successionEnds(Duration.ofMillis(100))
        .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));

    setCenter(codeArea);
  }

  private StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = pattern.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

    while (matcher.find()) {
      String styleClass =
          matcher.group("INSTRUCTION") != null
              ? "highlight-instruction"
              : matcher.group("BINARY") != null
                  ? "highlight-binary"
                  : matcher.group("ARGUMENT") != null
                      ? "highlight-value"
                      : null; /* never happens */
      assert styleClass != null;

      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  public void setCode(String code) {
    codeArea.clear();
    codeArea.appendText(code);
  }

  public ObservableValue<String> codeProperty() {
    return codeArea.textProperty();
  }
}
