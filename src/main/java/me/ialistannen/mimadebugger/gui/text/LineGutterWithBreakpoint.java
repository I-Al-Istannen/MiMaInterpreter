package me.ialistannen.mimadebugger.gui.text;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import org.fxmisc.richtext.CodeArea;

public class LineGutterWithBreakpoint implements IntFunction<Node> {

  private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
  private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
  private static final Font DEFAULT_FONT =
      Font.font("monospace", FontPosture.ITALIC, 13);
  private static final Background DEFAULT_BACKGROUND =
      new Background(new BackgroundFill(Color.web("#ddd"), null, null));

  private Set<Integer> breakpoints;
  private int gutterLineWidth;

  private Consumer<Integer> breakpointToggleListener;

  public LineGutterWithBreakpoint(CodeArea codeArea, Set<Integer> breakpoints,
      Consumer<Integer> breakpointToggleListener) {
    this.breakpoints = new HashSet<>(breakpoints);
    this.breakpointToggleListener = breakpointToggleListener;

    int lineCount = codeArea.getParagraphs().size();

    // calculate maximum size of the line number label to provide a constant width gutter
    this.gutterLineWidth = (int) Math.ceil(
        new Text(Integer.toString(lineCount))
            .getLayoutBounds().getWidth()
    );
  }

  @Override
  public Node apply(int lineNumber) {
    HBox container = new HBox();
    container.setFillHeight(true);
    container.setSpacing(5);
    container.setBackground(DEFAULT_BACKGROUND);
    container.setPadding(DEFAULT_INSETS);
    container.setMaxWidth(Double.MAX_VALUE);

    Label lineNo = new Label(Integer.toString(lineNumber));
    lineNo.setFont(DEFAULT_FONT);
    lineNo.setTextFill(DEFAULT_TEXT_FILL);
    lineNo.setAlignment(Pos.TOP_RIGHT);
    lineNo.getStyleClass().add("lineno");
    lineNo.setPrefWidth(gutterLineWidth);
    lineNo.setMaxWidth(gutterLineWidth);

    Circle breakpointIndicator = new Circle(4);
    breakpointIndicator.translateYProperty()
        .bind(
            container.heightProperty()
                .divide(2)
                .subtract(breakpointIndicator.getRadius())
        );
    updateIndicatorFill(breakpointIndicator, lineNumber);

    container.getChildren().add(lineNo);
    container.getChildren().add(breakpointIndicator);

    container.setOnMouseClicked(event -> breakpointToggleListener.accept(lineNumber));
    container.setCursor(Cursor.HAND);

    return container;
  }

  private void updateIndicatorFill(Circle circle, int line) {
    if (breakpoints.contains(line)) {
      circle.setFill(Color.ROYALBLUE);
    } else {
      circle.setFill(Color.TRANSPARENT);
    }
  }
}