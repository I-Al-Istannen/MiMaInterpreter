package me.ialistannen.mimadebugger.gui.state;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.gui.util.FxmlUtil;
import me.ialistannen.mimadebugger.gui.util.TableHelper;
import me.ialistannen.mimadebugger.machine.memory.Registers;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class RegisterView extends BorderPane {

  @FXML
  private TableView<RegisterValue> tableView;

  @FXML
  private TableColumn<RegisterValue, String> registerColumn;

  @FXML
  private TableColumn<RegisterValue, String> valueColumn;

  private ObservableList<RegisterValue> registers;

  public RegisterView() {
    this.registers = FXCollections.observableArrayList();

    FxmlUtil.loadWithRoot(this, "/gui/state/RegisterView.fxml");
  }

  @FXML
  void initialize() {
    registerColumn
        .setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
    valueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue()));

    tableView.setItems(registers);

    tableView.widthProperty().addListener(
        (observable, oldValue, newValue) -> TableHelper.autoSizeColumns(tableView)
    );
  }

  /**
   * Sets the {@link Registers} to display
   *
   * @param registers the registers to display
   */
  public void setRegisters(Registers registers) {
    this.registers.clear();

    this.registers.add(new RegisterValue("IP", registers.instructionPointer()));
    this.registers.add(new RegisterValue(
        "Instruction",
        MemoryFormat.toString(registers.instruction(), 24, false)
    ));
    this.registers.add(new RegisterValue(
        "Accumulator",
        registers.accumulator()
    ));
    this.registers.add(new RegisterValue(
        "Return address",
        registers.returnAddress()
    ));
    this.registers.add(new RegisterValue(
        "Stack pointer",
        registers.stackPointer()
    ));
    this.registers.add(new RegisterValue(
        "Frame pointer",
        registers.framePointer()
    ));

    TableHelper.autoSizeColumns(tableView);
  }

  /**
   * Clears this register display, to indicate that nothing is being displayed.
   */
  public void clearDisplay() {
    this.registers.clear();
  }

  /**
   * A single value of a single register.
   */
  private static class RegisterValue {

    private String name;
    private String value;

    RegisterValue(String name, String value) {
      this.name = name;
      this.value = value;
    }

    RegisterValue(String name, int value) {
      this(name, Integer.toString(value));
    }

    String getName() {
      return name;
    }

    String getValue() {
      return value;
    }
  }
}
