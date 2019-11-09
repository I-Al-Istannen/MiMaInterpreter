package me.ialistannen.mimadebugger.gui.menu;

import static java.nio.file.Files.readAllBytes;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuBar;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.swing.SwingUtilities;
import me.ialistannen.mimadebugger.fileio.MimaBinaryFormat;
import me.ialistannen.mimadebugger.fileio.MimaDisassembler;
import me.ialistannen.mimadebugger.gui.util.DialogUtil;
import me.ialistannen.mimadebugger.gui.util.FxmlUtil;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.util.MiMaExceptionSupplier;

public class Menubar extends MenuBar {

  private Consumer<String> programLoadedListener;
  private Supplier<String> codeSupplier;
  private Supplier<InstructionSet> instructionSetSupplier;
  private final MiMaExceptionSupplier<State> stateSupplier;

  public Menubar(Consumer<String> programLoadedListener,
      Supplier<String> codeSupplier,
      Supplier<InstructionSet> instructionSetSupplier,
      MiMaExceptionSupplier<State> stateSupplier) {
    this.programLoadedListener = programLoadedListener;
    this.codeSupplier = codeSupplier;
    this.instructionSetSupplier = instructionSetSupplier;
    this.stateSupplier = stateSupplier;

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
        programLoadedListener.accept(String.join("\n", lines));
      } catch (Exception e) {
        showErrorDialog("Error loading the file '" + file.getAbsolutePath() + "'", e);
      }
    }
  }

  @FXML
  private void onLoadBinary() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Please choose a mima binary file");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("All", "*"));
    fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

    File file = fileChooser.showOpenDialog(getScene().getWindow());

    if (file != null) {
      try {
        programLoadedListener.accept(
            new MimaDisassembler().fromAssembly(
                readAllBytes(file.toPath()), instructionSetSupplier.get()
            )
        );
      } catch (Exception e) {
        showErrorDialog("Error loading the file '" + file.getAbsolutePath() + "'", e);
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
    alert.initOwner(getScene().getWindow());
    alert.show();
  }

  @FXML
  private void onSave() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choose a file to save to");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("MiMa files", "*.mima"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("All files", "*"));
    fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

    File file = fileChooser.showSaveDialog(getScene().getWindow());

    if (file == null) {
      return;
    }

    try {
      List<String> lines = Arrays.asList(codeSupplier.get().split("\n"));
      Files.write(file.toPath(), lines);
    } catch (Exception e) {
      showErrorDialog("Error saving file to '" + file.getAbsolutePath() + "'", e);
    }
  }

  @FXML
  private void onSaveBinary() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choose a file to save the binary version to");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("MiMa binary files", "*.mima"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("All files", "*"));
    fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

    File file = fileChooser.showSaveDialog(getScene().getWindow());

    if (file == null) {
      return;
    }

    try {
      Files.write(
          file.toPath(),
          new MimaBinaryFormat().save(stateSupplier.get())
      );
    } catch (Exception e) {
      showErrorDialog("Error saving binary file to '" + file.getAbsolutePath() + "'", e);
    }
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
      } catch (Exception e) {
        Platform.runLater(() ->
            showErrorDialog("Error opening a link to '" + url + "'", e)
        );
      }
    }));

    return hyperlink;
  }

  private void showErrorDialog(String headerText, Throwable e) {
    DialogUtil.showErrorDialog("An error occurred", headerText, getScene().getWindow(), e);
  }
}
