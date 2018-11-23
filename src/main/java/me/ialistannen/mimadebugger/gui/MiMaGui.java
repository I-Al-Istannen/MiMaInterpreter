package me.ialistannen.mimadebugger.gui;

import static me.ialistannen.mimadebugger.machine.MiMa.readResource;

import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.ialistannen.mimadebugger.gui.execution.ExecutionControls;
import me.ialistannen.mimadebugger.gui.state.ImmutableEncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.MemoryView;
import me.ialistannen.mimadebugger.gui.text.ProgramTextPane;
import me.ialistannen.mimadebugger.machine.ImmutableState;
import me.ialistannen.mimadebugger.machine.MiMa;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.machine.program.ProgramParser;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class MiMaGui extends Application {

  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();

    MemoryView memoryView = new MemoryView();

    InstructionSet instructionSet = new InstructionSet();

    memoryView.setMemoryValueDecoder((address, value) -> ImmutableEncodedInstructionCall.builder()
        .address(address)
        .representation(MemoryFormat.toString(value, 24, false))
        .instructionCall(instructionSet.forEncodedValue(value))
        .build()
    );

    ProgramParser parser = new ProgramParser(instructionSet);
    List<InstructionCall> calls = parser.parseFromNames(readResource("/AddOne.mima"));

    MainMemory memory = MainMemory.create();

    for (int i = 0; i < calls.size(); i++) {
      InstructionCall call = calls.get(i);
      memory = memory.set(
          i,
          MemoryFormat.combineInstruction(call.command().opcode(), call.argument())
      );
    }

    memoryView.setMemory(memory);

    SplitPane split = new SplitPane(new ProgramTextPane(), memoryView);
    split.setDividerPositions(0.8);
    root.setCenter(split);

    ExecutionControls executionControls = new ExecutionControls();
    MiMaRunner runner = new MiMaRunner(
        new MiMa(
            ImmutableState.builder()
                .registers(
                    ImmutableRegisters.builder()
                        .build()
                )
                .memory(memory)
                .build(),
            new InstructionSet()
        )
    );

    executionControls.setRunner(runner);

    executionControls.setStateConsumer(state -> memoryView.setMemory(state.memory()));

    root.setLeft(executionControls);

    primaryStage.setScene(new Scene(root));
    primaryStage.sizeToScene();
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
