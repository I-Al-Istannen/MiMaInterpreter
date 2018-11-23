package me.ialistannen.mimadebugger.gui.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import me.ialistannen.mimadebugger.gui.state.EncodedInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;

public class HighlightedTextFlow extends TextFlow {

  public HighlightedTextFlow(EncodedInstructionCall call) {
    super(getTextsForCall(call));
  }

  private static Text[] getTextsForCall(EncodedInstructionCall encodedInstructionCall) {
    List<Text> texts = new ArrayList<>();

    texts.add(getTextForBinaryRepresentation(encodedInstructionCall.representation()));
    texts.addAll(getTextsForInstruction(encodedInstructionCall.instructionCall()));

    return texts.toArray(new Text[0]);
  }

  private static Text getTextForBinaryRepresentation(String representation) {
    Text text = new Text(representation + " ");
    text.setFont(Font.font("monospaced"));
    text.setFill(Color.BLACK);
    return text;
  }

  private static List<Text> getTextsForInstruction(InstructionCall call) {
    Text name = new Text(String.format("%-4s ", call.command().name()));
    name.setFont(Font.font("monospaced"));
    name.setFill(Color.TOMATO);

    Text argument = new Text(String.valueOf(call.argument()));
    argument.setFont(Font.font("monospaced"));
    argument.setFill(Color.ROYALBLUE);

    return Arrays.asList(name, argument);
  }
}
