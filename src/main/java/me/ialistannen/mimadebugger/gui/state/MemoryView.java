package me.ialistannen.mimadebugger.gui.state;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.gui.highlighting.HighlightedTextFlow;

public class MemoryView extends BorderPane {

  @FXML
  private JFXListView<EncodedInstructionCall> memoryList;

  private ObservableList<EncodedInstructionCall> memory;

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
    memoryList.setCellFactory(param -> new JFXListCell<EncodedInstructionCall>() {
      @Override
      protected void updateItem(EncodedInstructionCall item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);

        if (item == null || empty) {
          setGraphic(null);
          return;
        }

        setGraphic(new HighlightedTextFlow(item));
      }
    });

    memoryList.setItems(memory);
  }

  /**
   * Sets the memory the pane displays.
   *
   * @param memory the memory the pane displays
   */
  public void setMemory(List<EncodedInstructionCall> memory) {
    this.memory.setAll(memory);
  }
}
