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
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.gui.highlighting.HighlightedMemoryValue;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;

public class MemoryView extends BorderPane {

  @FXML
  private TableView<MemoryValue> memoryTable;

  private ObservableList<MemoryValue> memory;

  private BiFunction<Integer, Integer, MemoryValue> memoryValueDecoder;

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
    getStylesheets().add("/css/Highlight.css");

    TableColumn<MemoryValue, Number> addressColumn = column("Address", MemoryValue::address);

    TableColumn<MemoryValue, MemoryValue> valueColumn = column("Value", x -> x);
    valueColumn.setCellFactory(
        param -> cell(
            (value, cell) -> cell.setGraphic(new HighlightedMemoryValue(value))
        )
    );

    valueColumn.prefWidthProperty().bind(
        memoryTable.widthProperty()
            .subtract(addressColumn.widthProperty())
            .subtract(2)
    );

    memoryTable.getColumns().add(addressColumn);
    memoryTable.getColumns().add(valueColumn);

    memoryTable.setItems(memory);
    // prevent resizing
    memoryTable.setColumnResizePolicy(param -> true);
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
}
