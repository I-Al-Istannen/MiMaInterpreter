package me.ialistannen.mimadebugger.gui.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public final class TableHelper {

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
   * This method takes care of all the default emptying and resetting of the cell.
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
          setGraphic(null);
          setText(null);
          return;
        }

        consumer.accept(item, this);
      }
    };
  }

}
