package me.ialistannen.mimadebugger.gui.execution;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.State;

public class ExecutionControls extends BorderPane {

  private MiMaRunner runner;
  private Consumer<State> stateConsumer;

  public ExecutionControls() {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/gui/execution/ExecutionControls.fxml")
    );

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setRunner(MiMaRunner runner) {
    this.runner = runner;
  }

  public void setStateConsumer(Consumer<State> stateConsumer) {
    this.stateConsumer = stateConsumer;
  }

  @FXML
  private void onNext() {
    stateConsumer.accept(runner.nextStep());
  }

  @FXML
  private void onPrevious() {
    stateConsumer.accept(runner.previousStep());
  }

  @FXML
  private void onExecute() {
    try {
      while (true) {
        stateConsumer.accept(runner.nextStep());
      }
    } catch (ProgramHaltException ignored) {
    }
  }
}
