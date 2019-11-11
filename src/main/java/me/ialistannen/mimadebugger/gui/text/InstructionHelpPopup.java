package me.ialistannen.mimadebugger.gui.text;

import java.util.function.Function;
import java.util.function.Predicate;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;

public class InstructionHelpPopup extends Popup {

  private VBox parent;

  public InstructionHelpPopup() {
    parent = new VBox();
    parent.setFillWidth(false);
    parent.getStylesheets().add("/css/InstructionHelpPopup.css");
    parent.getStyleClass().add("root");

    getContent().add(parent);
  }

  private void setInstruction(Instruction instruction) {
    parent.getChildren().clear();
    Label instructionLabel = labelWithClass(instruction.name(), "instruction");
    parent.getChildren().add(instructionLabel);

    Separator separator = new Separator(Orientation.HORIZONTAL);
    separator.getStyleClass().add("separator");
    separator.prefWidthProperty().bind(instructionLabel.widthProperty().add(15));
    parent.getChildren().add(separator);

    instruction.description().ifPresent(description -> parent.getChildren().add(
        labelWithClass(description, "description")
    ));
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
   * @param instructionSet the instruction set to use
   */
  public static void attachTo(StyledTextArea<?, ?> area, InstructionSet instructionSet) {
    InstructionHelpPopup popup = new InstructionHelpPopup();

    area.addEventHandler(
        MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN,
        event -> {
          String hoveredWord = findWordAroundCursor(area.getText(), event);
          instructionSet.forName(hoveredWord).ifPresent(instruction -> {
            popup.setInstruction(instruction);
            popup.show(
                area,
                event.getScreenPosition().getX(),
                event.getScreenPosition().getY() + 10
            );
          });
        }
    );
    area.addEventHandler(
        MouseOverTextEvent.MOUSE_OVER_TEXT_END,
        event -> popup.hide()
    );
  }

  private static String findWordAroundCursor(String text, MouseOverTextEvent event) {
    int wordEnd = findNextOccurrence(
        text,
        event.getCharacterIndex(),
        Character::isWhitespace,
        i -> i + 1
    );
    // string end is exclusive, so add one
    wordEnd = Math.min(wordEnd + 1, text.length());

    int wordStart = findNextOccurrence(
        text,
        event.getCharacterIndex(),
        Character::isWhitespace,
        i -> i - 1
    );
    return text.substring(wordStart, wordEnd);
  }

  private static int findNextOccurrence(String text, int start,
      Predicate<Character> predicate, Function<Integer, Integer> update) {

    int i = start;

    int previousI = i;
    for (; i < text.length() && i >= 0; i = update.apply(i)) {
      if (predicate.test(text.charAt(i))) {
        // Do not return the occurrence, but the position before
        i = previousI;
        break;
      }
      previousI = i;
    }

    return Math.max(0, i);
  }
}
