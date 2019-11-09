package me.ialistannen.mimadebugger.gui.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.Window;

public class DialogUtil {

  /**
   * Shows an error dialog.
   *
   * @param title the dialog title
   * @param headerText the header text
   * @param owner the owning window
   * @param e the exception that occurred
   */
  public static void showErrorDialog(String title, String headerText, Window owner, Throwable e) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(headerText);
    alert.initOwner(owner);

    StringWriter stringWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stringWriter));

    TextArea textArea = new TextArea(stringWriter.toString());
    textArea.setFont(Font.font("monospace"));

    alert.getDialogPane().setExpandableContent(textArea);

    alert.show();
  }

}
