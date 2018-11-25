package me.ialistannen.mimadebugger.gui.menu;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.swing.SwingUtilities;
import me.ialistannen.mimadebugger.gui.util.FxmlUtil;

public class Menubar extends MenuBar {

  private Consumer<List<String>> programLoadedListener;

  public Menubar(Consumer<List<String>> programLoadedListener) {
    this.programLoadedListener = programLoadedListener;

    FxmlUtil.loadWithRoot(this, "/gui/menu/MenuBar.fxml");
  }

  @FXML
  private void onLoad() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Please choose a mima file");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("All", "*"));
    fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

    File file = fileChooser.showOpenDialog(getScene().getWindow());

    if (file != null) {
      try {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        programLoadedListener.accept(lines);
      } catch (IOException e) {
        createErrorDialog("Error loading the file '" + file.getAbsolutePath() + "'", e);
      }
    }
  }

  @FXML
  private void onQuit() {
    Platform.exit();
  }

  @FXML
  private void onAbout() {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("About this program");
    alert.setHeaderText(
        "This is a Mi(nimal)Ma(schine) Interpreter written in Java by I Al Istannen"
    );
    alert.getDialogPane().setContent(new TextFlow(
        new Text("You can find the project on"),
        link("github", "https://github.com/I-Al-Istannen/MiMaInterpreter"),
        new Text("and you are encouraged to post bugs or feature requests into the"),
        link("issues", "https://github.com/I-Al-Istannen/MiMaInterpreter/issues"),
        new Text("section there!")
    ));
    alert.show();
  }

  private Node link(String label, String url) {
    Hyperlink hyperlink = new Hyperlink(label);

    // Why has JavafX *still* no HostServices delegate on Linux? And why does Desktop.open freeze
    // the fx thraed?
    hyperlink.setOnAction(event -> SwingUtilities.invokeLater(() -> {
      try {
        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().browse(URI.create(url));
        }
      } catch (IOException e) {
        Platform.runLater(() ->
            createErrorDialog("Error opening a link to '" + url + "'", e)
                .show()
        );
      }
    }));

    return hyperlink;
  }

  private static Alert createErrorDialog(String headerText, Throwable e) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("An error occurred");
    alert.setHeaderText(headerText);

    StringWriter stringWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stringWriter));

    TextArea textArea = new TextArea(stringWriter.toString());
    textArea.setFont(Font.font("monospace"));

    alert.getDialogPane().setExpandableContent(textArea);

    return alert;
  }
}
