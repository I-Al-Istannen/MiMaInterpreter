package me.ialistannen.mimadebugger.gui.state;

import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
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

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/state/RegisterView.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  void initialize() {
    registerColumn
        .setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
    valueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue()));

    tableView.setItems(registers);
  }

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

    System.out.println("Added: " + registers);
  }

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
