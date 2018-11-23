package me.ialistannen.mimadebugger.gui.text;

import com.jfoenix.controls.JFXTextArea;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

public class ProgramTextPane extends BorderPane {

  @FXML
  private JFXTextArea textArea;

  public ProgramTextPane() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/text/ProgramTextPane.fxml"));

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
    textArea.appendText("Hello world");
  }
}
