package me.ialistannen.mimadebugger.gui.state;

import java.util.function.BiFunction;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import me.ialistannen.mimadebugger.gui.util.FxmlUtil;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;

public class StateView extends VBox {

  @FXML
  private RegisterView registerView;

  @FXML
  private MemoryView memoryView;

  public StateView() {
    FxmlUtil.loadWithRoot(this, "/gui/state/StateView.fxml");
  }

  @FXML
  private void initialize() {
    VBox.setVgrow(memoryView, Priority.ALWAYS);
    VBox.setVgrow(registerView, Priority.SOMETIMES);

    setPadding(new Insets(10));
  }

  /**
   * The decoder vor memory values. It receives an address and the value at this address and returns
   * a {@link MemoryValue}.
   *
   * @param decoder the decoder
   */
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
      memoryView.setCurrentInstructionPointerAddress(-1);
      registerView.clearDisplay();
    } else {
      memoryView.setMemory(state.memory());
      memoryView.setCurrentInstructionPointerAddress(state.registers().instructionPointer());
      registerView.setRegisters(state.registers());
    }
  }
}
