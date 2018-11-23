package me.ialistannen.mimadebugger.gui.state;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.Function;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.gui.highlighting.HighlightedTextFlow;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class MemoryView extends BorderPane {

  @FXML
  private TableView<EncodedInstructionCall> memoryTable;

  private ObservableList<EncodedInstructionCall> memory;

  private Function<Integer, InstructionCall> memoryDecoder;

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
    TableColumn<EncodedInstructionCall, Number> addressColumn = new TableColumn<>("Address");
    addressColumn
        .setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().address()));

    TableColumn<EncodedInstructionCall, EncodedInstructionCall> valueColumn
        = new TableColumn<>("Value");
    valueColumn
        .setCellFactory(param -> new TableCell<EncodedInstructionCall, EncodedInstructionCall>() {
          @Override
          protected void updateItem(EncodedInstructionCall item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
              setGraphic(null);
              setText(null);
              return;
            }

            HighlightedTextFlow value = new HighlightedTextFlow(item);
            setGraphic(value);
          }
        });
    valueColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));

    memoryTable.getColumns().add(addressColumn);
    memoryTable.getColumns().add(valueColumn);

    memoryTable.setItems(memory);
    memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
  }

  public void setMemoryDecoder(Function<Integer, InstructionCall> memoryDecoder) {
    this.memoryDecoder = memoryDecoder;
  }

  /**
   * Sets the memory the pane displays.
   *
   * @param memory the memory the pane displays
   */
  public void setMemory(MainMemory memory) {
    this.memory.clear();

    for (Entry<Integer, Integer> entry : memory.getMemory().entrySet()) {
      InstructionCall call = memoryDecoder.apply(entry.getValue());
      this.memory.add(
          ImmutableEncodedInstructionCall.builder()
              .instructionCall(call)
              .representation(MemoryFormat.toString(entry.getValue(), 24, false))
              .address(entry.getKey())
              .build()
      );
    }
  }
}
