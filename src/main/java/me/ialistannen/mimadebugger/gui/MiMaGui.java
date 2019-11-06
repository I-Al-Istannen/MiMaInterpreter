package me.ialistannen.mimadebugger.gui;

import static java.util.stream.Collectors.toList;

import javafx.application.Application;
import javafx.collections.SetChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import me.ialistannen.mimadebugger.gui.execution.ExecutionControls;
import me.ialistannen.mimadebugger.gui.menu.Menubar;
import me.ialistannen.mimadebugger.gui.state.ImmutableEncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.StateView;
import me.ialistannen.mimadebugger.gui.text.ProgramTextPane;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;

public class MiMaGui extends Application {

  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();

    InstructionSet instructionSet = new InstructionSet();
    StateView stateView = new StateView();

    stateView.setMemoryValueDecoder((address, value) -> ImmutableEncodedInstructionCall.builder()
        .address(address)
        .representation(value)
        .instructionCall(instructionSet.forEncodedValue(value))
        .build()
    );

    ExecutionControls executionControls = new ExecutionControls(instructionSet);
    executionControls.setStateConsumer(stateView::setState);

    ProgramTextPane programTextPane = new ProgramTextPane(
        instructionSet.getAll().stream()
            .map(Instruction::name)
            .collect(toList())
    );
    programTextPane.getBreakpoints().addListener((SetChangeListener<Integer>) change -> {
      if (change.wasAdded()) {
        executionControls.addBreakpoint(change.getElementAdded());
      }
      if (change.wasRemoved()) {
        executionControls.removeBreakpoint(change.getElementRemoved());
      }
    });
    executionControls.programTextPropertyProperty().bind(programTextPane.codeProperty());

    Menubar menubar = new Menubar(
        readLines -> programTextPane.setCode(String.join("\n", readLines)),
        () -> programTextPane.codeProperty().getValue(),
        executionControls::getProgramMemoryLayout
    );

    HBox mainPane = new HBox(programTextPane, stateView);
    HBox.setHgrow(programTextPane, Priority.ALWAYS);

    root.setTop(menubar);
    root.setCenter(mainPane);
    root.setLeft(executionControls);

    Scene scene = new Scene(root);
    scene.getStylesheets().add("/css/Base.css");
    primaryStage.setScene(scene);
    primaryStage.setHeight(500);
    primaryStage.setWidth(1000);
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
