package me.ialistannen.mimadebugger.gui.util;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class TableHelper {

  private static final Logger LOGGER = Logger.getLogger("TableHelper");

  private TableHelper() {
    throw new UnsupportedOperationException("No instantiation");
  }

  /**
   * Creates a column with the given name and the provided value extraction function. It will create
   * a new {@link SimpleObjectProperty} for the result of the value function.
   *
   * @param name the name of the column
   * @param valueFunction the function used to extract a value from the row object
   * @param <S> the type of the row object
   * @param <T> the type of the column's result
   * @return a column with the given name and value extraction function
   */
  public static <S, T> TableColumn<S, T> column(String name, Function<S, T> valueFunction) {
    TableColumn<S, T> column = new TableColumn<>(name);

    column.setCellValueFactory(
        param -> new SimpleObjectProperty<>(valueFunction.apply(param.getValue()))
    );

    return column;
  }

  /**
   * Creates a new {@link TableCell} with the given mutation function.
   *
   * This method takes care of all the default emptying and resetting of the cell,
   * <em>but does not null the graphic.</em>
   *
   * @param consumer the consumer to invoke. It takes the item and the cell as inputs
   * @param <S> the type of the row object
   * @param <T> the type of the column's result
   * @return a new table cell with the given mutation function
   */
  public static <S, T> TableCell<S, T> cell(BiConsumer<T, TableCell<S, T>> consumer) {
    return new TableCell<S, T>() {
      @Override
      protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
          setText(null);
          return;
        }

        consumer.accept(item, this);
      }
    };
  }

  /**
   * Sizes the columns of the table view to fit.
   *
   * @param tableView the table view to resize
   */
  public static void autoSizeColumns(TableView<?> tableView) {
    try {
      Method method = TableViewSkin.class
          .getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
      method.setAccessible(true);
      for (TableColumn<?, ?> column : tableView.getColumns()) {
        method.invoke(tableView.getSkin(), column, -1);
      }
    } catch (ReflectiveOperationException e) {
      LOGGER.log(Level.WARNING, "Error setting tableview size", e);
    }
  }

}
