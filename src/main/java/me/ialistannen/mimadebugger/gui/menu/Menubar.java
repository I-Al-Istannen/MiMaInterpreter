package me.ialistannen.mimadebugger.gui.menu;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import me.ialistannen.mimadebugger.fileio.MimaBinaryFormat;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.gui.util.FxmlUtil;
import me.ialistannen.mimadebugger.machine.State;

public class Menubar extends MenuBar {

  private Consumer<List<String>> programLoadedListener;
  private final Consumer<List<Integer>> programBinaryLoadedListener;
  private Supplier<String> codeSupplier;
  private final Supplier<List<MemoryValue>> memorySupplier;

  public Menubar(Consumer<List<String>> programLoadedListener,
      Consumer<List<Integer>> programBinaryLoadedListener,
      Supplier<String> codeSupplier,
      Supplier<List<MemoryValue>> memorySupplier) {
    this.programLoadedListener = programLoadedListener;
    this.programBinaryLoadedListener = programBinaryLoadedListener;
    this.codeSupplier = codeSupplier;
    this.memorySupplier = memorySupplier;

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
        State loaded = new MimaBinaryFormat().load(Files.readAllBytes(file.toPath()));
        programBinaryLoadedListener.accept(
            loaded.memory().getMemory().entrySet().stream()
                .sorted(Entry.comparingByKey())
                .map(Entry::getValue)
                .collect(Collectors.toList())
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
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      // TODO: Implement
      // register dummy
      out.write(new byte[5 * 3]);

      int currentAddress = 0;
      List<MemoryValue> values = memorySupplier.get().stream()
          .sorted(Comparator.comparing(MemoryValue::address))
          .collect(Collectors.toList());

      for (MemoryValue value : values) {
        // pad with zeros until next occupied address
        while (value.address() > currentAddress) {
          currentAddress++;
          out.write(new byte[]{0, 0, 0});
        }
        out.write(value.representation() >>> 16 & 0xFF);
        out.write(value.representation() >>> 8 & 0xFF);
        out.write(value.representation() & 0xFF);
        currentAddress++;
      }
      Files.write(file.toPath(), out.toByteArray());
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
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("An error occurred");
    alert.setHeaderText(headerText);
    alert.initOwner(getScene().getWindow());

    StringWriter stringWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stringWriter));

    TextArea textArea = new TextArea(stringWriter.toString());
    textArea.setFont(Font.font("monospace"));

    alert.getDialogPane().setExpandableContent(textArea);

    alert.show();
  }
}
