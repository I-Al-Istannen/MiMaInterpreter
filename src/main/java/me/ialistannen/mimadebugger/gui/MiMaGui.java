package me.ialistannen.mimadebugger.gui;

import static me.ialistannen.mimadebugger.machine.MiMa.readResource;

import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.ialistannen.mimadebugger.gui.state.EncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.ImmutableEncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.MemoryView;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.program.ProgramParser;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class MiMaGui extends Application {

  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();

    MemoryView memoryView = new MemoryView();

    ProgramParser parser = new ProgramParser(new InstructionSet());
    List<InstructionCall> calls = parser.parseFromNames(readResource("/AddOne.mima"));

    List<EncodedInstructionCall> instructions = calls.stream()
        .map(call -> ImmutableEncodedInstructionCall.builder()
            .instructionCall(call)
            .representation(
                MemoryFormat.toString(
                    MemoryFormat.combineInstruction(call.command().opcode(), call.argument()),
                    24,
                    false
                )
            )
            .build()
        )
        .collect(Collectors.toList());

    memoryView.setMemory(instructions);

    root.setCenter(memoryView);

    primaryStage.setScene(new Scene(root));
    primaryStage.setWidth(500);
    primaryStage.setHeight(500);
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
