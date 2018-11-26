package me.ialistannen.mimadebugger.gui.highlighting;

import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class HighlightedMemoryValue extends TextFlow {

  private Text binary;
  private Text instruction;
  private Text argument;

  public HighlightedMemoryValue(MemoryValue value) {
    binary = createText(HighlightingCategory.BINARY);
    instruction = createText(HighlightingCategory.INSTRUCTION);
    argument = createText(HighlightingCategory.VALUE);

    getChildren().addAll(binary, instruction, argument);

    initForValue(value);
    Platform.runLater(() -> setPrefHeight(computeMinHeight(getPrefWidth())));
  }

  private void initForValue(MemoryValue memoryValue) {
    setBinaryRepresentation(memoryValue.representation());

    if (memoryValue.instructionCall().isPresent()) {
      setTextForInstruction(memoryValue.instructionCall().get());
    } else {
      clearInstructionTexts();
    }

    initHeight();
  }

  private void initHeight() {
    setPrefHeight(computeMinHeight(getWidth()));
  }

  private void setBinaryRepresentation(int representation) {
    binary.setText(MemoryFormat.toString(representation, 24, false) + " ");
  }

  private void setTextForInstruction(InstructionCall call) {
    instruction.setText(String.format("%-4s ", call.command().name()));
    argument.setText(String.valueOf(call.argument()));
  }

  private void clearInstructionTexts() {
    instruction.setText("");
    argument.setText("");
  }

  private Text createText(HighlightingCategory category) {
    Text textNode = new Text();

    textNode.getStyleClass().add(category.getCssClass());
    textNode.setFont(Font.font("monospaced"));

    return textNode;
  }

  /**
   * Sets the value to be displayed.
   *
   * @param value the value to display
   */
  public void setValue(MemoryValue value) {
    initForValue(value);
  }

  /**
   * Clears this node so that nothing is displayed.
   */
  public void clear() {
    binary.setText("");
    instruction.setText("");
    argument.setText("");
  }
}
