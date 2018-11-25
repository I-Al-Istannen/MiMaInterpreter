package me.ialistannen.mimadebugger.gui.util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class FxmlUtil {

  /**
   * Loads a FXML file with the given node as root and controller.
   *
   * @param root the node to use as root and controller
   * @param url the absolute url the FXML file can be found in
   */
  public static void loadWithRoot(Node root, String url) {
    FXMLLoader loader = new FXMLLoader(FxmlUtil.class.getResource(url));
    loader.setRoot(root);
    loader.setController(root);

    try {
      loader.load();
    } catch (IOException e) {
      throw new FxmlLoadException("Error loading '" + url + "'", e);
    }
  }
}
