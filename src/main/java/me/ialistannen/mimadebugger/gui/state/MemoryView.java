package me.ialistannen.mimadebugger.gui.state;

import static me.ialistannen.mimadebugger.gui.util.TableHelper.cell;
import static me.ialistannen.mimadebugger.gui.util.TableHelper.column;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.gui.highlighting.HighlightedMemoryValue;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;

public class MemoryView extends BorderPane {

  @FXML
  private TableView<MemoryValue> memoryTable;

  private ObservableList<MemoryValue> memory;

  private BiFunction<Integer, Integer, MemoryValue> memoryValueDecoder;
  private int instructionPointerAddress = -1;

  public MemoryView() {
    this.memory = FXCollections.observableArrayList();

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/state/MemoryView.fxml"));

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
    TableColumn<MemoryValue, Number> addressColumn = column("Address", MemoryValue::address);
    addressColumn.setPrefWidth(90);

    TableColumn<MemoryValue, MemoryValue> valueColumn = column("Value", x -> x);
    valueColumn.setCellFactory(
        param -> cell(
            (value, cell) -> cell.setGraphic(new HighlightedMemoryValue(value))
        )
    );
    valueColumn.setPrefWidth(280);

    memoryTable.setRowFactory(param -> new TableRow<MemoryValue>() {
      @Override
      protected void updateItem(MemoryValue item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty || item.address() != instructionPointerAddress) {
          getStyleClass().remove("current-instruction-pointer-address");
          return;
        }

        if (item.address() == instructionPointerAddress) {
          getStyleClass().add("current-instruction-pointer-address");
        }
      }
    });

    memoryTable.getColumns().add(addressColumn);
    memoryTable.getColumns().add(valueColumn);

    memoryTable.setItems(memory);
    memoryTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
  }

  public void setMemoryValueDecoder(BiFunction<Integer, Integer, MemoryValue> decoder) {
    this.memoryValueDecoder = decoder;
  }

  /**
   * Sets the memory the pane displays.
   *
   * @param memory the memory the pane displays
   */
  public void setMemory(MainMemory memory) {
    this.memory.clear();

    for (Entry<Integer, Integer> memoryEntry : memory.getMemory().entrySet()) {
      int address = memoryEntry.getKey();
      int value = memoryEntry.getValue();

      MemoryValue storedValue = memoryValueDecoder.apply(address, value);

      this.memory.add(storedValue);
    }
  }

  /**
   * Sets the current address of the instruction pointer.
   *
   * @param address the current address of the instruction pointer
   */
  public void setCurrentInstructionPointerAddress(int address) {
    this.instructionPointerAddress = address;
  }
}
