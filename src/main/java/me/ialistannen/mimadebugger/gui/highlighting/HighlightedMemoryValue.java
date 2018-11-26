package me.ialistannen.mimadebugger.gui.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class HighlightedMemoryValue extends TextFlow {

  public HighlightedMemoryValue(MemoryValue call) {
    getChildren().addAll(getTextsForCall(call));

    // Update on the first pulse after this component is visible
    Platform.runLater(() -> setPrefHeight(computeMinHeight(getWidth())));

    widthProperty().addListener((observable, oldValue, newValue) ->
        setPrefHeight(computeMinHeight(newValue.doubleValue()))
    );
  }

  private Collection<Text> getTextsForCall(MemoryValue memoryValue) {
    List<Text> texts = new ArrayList<>();

    texts.add(getTextForBinaryRepresentation(memoryValue.representation()));

    memoryValue.instructionCall()
        .ifPresent(call -> texts.addAll(getTextsForInstruction(call)));

    return texts;
  }

  private Text getTextForBinaryRepresentation(int representation) {
    return createText(
        MemoryFormat.toString(representation, 24, false) + " ",
        HighlightingCategory.BINARY
    );
  }

  private List<Text> getTextsForInstruction(InstructionCall call) {
    return Arrays.asList(
        createText(String.format("%-4s ", call.command().name()), HighlightingCategory.INSTRUCTION),
        createText(String.valueOf(call.argument()), HighlightingCategory.VALUE)
    );
  }

  private Text createText(String text, HighlightingCategory category) {
    Text textNode = new Text(text);

    textNode.getStyleClass().add(category.getCssClass());
    textNode.setFont(Font.font("monospaced"));

    return textNode;
  }
}
