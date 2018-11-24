package me.ialistannen.mimadebugger.gui.menu;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Menubar extends MenuBar {

  private Consumer<List<String>> programLoadedListener;

  public Menubar(Consumer<List<String>> programLoadedListener) {
    this.programLoadedListener = programLoadedListener;

    Menu fileMenu = new Menu("File");
    fileMenu.getItems().add(item("Quit", Platform::exit));

    fileMenu.getItems().add(item("Load", this::loadProgram));

    getMenus().add(fileMenu);
  }

  private MenuItem item(String name, Runnable action) {
    MenuItem menuItem = new MenuItem(name);

    menuItem.setOnAction(event -> action.run());

    return menuItem;
  }

  private void loadProgram() {
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
        e.printStackTrace();
      }
    }
  }
}
