package me.ialistannen.mimadebugger.gui.state;

import java.io.IOException;
import java.util.function.BiFunction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;

public class StateView extends VBox {

  @FXML
  private RegisterView registerView;

  @FXML
  private MemoryView memoryView;

  public StateView() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/state/StateView.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  private void initialize() {
    VBox.setVgrow(memoryView, Priority.ALWAYS);
    VBox.setVgrow(registerView, Priority.SOMETIMES);

    setPadding(new Insets(10));
  }

  public void setMemoryValueDecoder(BiFunction<Integer, Integer, MemoryValue> decoder) {
    memoryView.setMemoryValueDecoder(decoder);
  }

  /**
   * The state to display in this view.
   *
   * @param state the state to display, null for nothing
   */
  public void setState(State state) {
    if (state == null) {
      memoryView.setMemory(MainMemory.create());
      registerView.clearDisplay();
    } else {
      memoryView.setMemory(state.memory());
      registerView.setRegisters(state.registers());
    }
  }
}
