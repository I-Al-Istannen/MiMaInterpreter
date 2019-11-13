package me.ialistannen.mimadebugger.gui.text;

import java.util.Optional;
import java.util.function.Function;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import me.ialistannen.mimadebugger.parser.validation.ParsingProblem;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;

/**
 * A popup that displays syntax errors.
 */
public class SyntaxErrorPopup extends Popup {

  private VBox parent;

  public SyntaxErrorPopup() {
    parent = new VBox();
    parent.setFillWidth(false);
    parent.getStylesheets().add("/css/SyntaxErrorPopup.css");
    parent.getStyleClass().add("root");

    getContent().add(parent);
  }

  private void setError(ParsingProblem error) {
    parent.getChildren().clear();
    parent.getChildren().add(
        labelWithClass(error.message(), "syntax-error")
    );
  }

  private Label labelWithClass(String text, String... styleClasses) {
    Label label = new Label(text);
    label.getStyleClass().addAll(styleClasses);
    return label;
  }

  /**
   * Attaches this help popup to a styled text area.
   *
   * @param area the styled text area
   * @param errorFunction the function that extracts errors from the character position
   */
  public static void attachTo(StyledTextArea<?, ?> area,
      Function<Integer, Optional<ParsingProblem>> errorFunction) {
    SyntaxErrorPopup popup = new SyntaxErrorPopup();

    area.addEventHandler(
        MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN,
        event -> errorFunction.apply(event.getCharacterIndex()).ifPresent(error -> {
          popup.setError(error);
          popup.show(
              area,
              event.getScreenPosition().getX(),
              event.getScreenPosition().getY() + 10
          );
        })
    );
    area.addEventHandler(
        MouseOverTextEvent.MOUSE_OVER_TEXT_END,
        event -> popup.hide()
    );

    area.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
        errorFunction.apply(area.getCaretPosition()).ifPresent(syntaxError -> {
          Optional<Bounds> caretBounds = area.getCaretBounds();
          if (!caretBounds.isPresent()) {
            return;
          }
          double x = caretBounds.get().getMaxX();
          double y = caretBounds.get().getMaxY();
          popup.setError(syntaxError);
          popup.show(area, x, y);
        });
      } else {
        popup.hide();
      }
    });
  }
}
