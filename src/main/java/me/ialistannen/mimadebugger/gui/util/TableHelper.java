package me.ialistannen.mimadebugger.gui.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import me.ialistannen.mimadebugger.gui.util.AutoSizableTableViewSkin.ColumnHeader;

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
   * Creates a new {@link TableCell} with the given mutation function. This method takes care of all
   * the default emptying and resetting of the cell,
   * <em>but does not null the graphic.</em>
   *
   * @param creation the supplier to create a graphic
   * @param clear the consumer to clear a graphic's value
   * @param set the function to set a graphic's value
   * @param <S> the type of the row object
   * @param <T> the type of the column's result
   * @param <D> the type of the graphic
   * @return a new table cell with the given mutation function
   */
  public static <S, T, D extends Node> TableCell<S, T> cell(Function<T, D> creation,
      Consumer<D> clear, BiConsumer<T, D> set) {
    return new TableCell<>() {
      @Override
      protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        @SuppressWarnings("unchecked")
        D graphic = (D) getGraphic();

        if (item == null || empty) {
          setText(null);
          if (graphic != null) {
            clear.accept(graphic);
          }
          return;
        }

        if (graphic == null) {
          graphic = creation.apply(item);
          setGraphic(graphic);
        }

        set.accept(item, graphic);
      }
    };
  }

  /**
   * Sizes the columns of the table view to fit.
   *
   * @param tableView the table view to resize
   */
  public static void autoSizeColumns(TableView<?> tableView) {
    for (TableColumn<?, ?> column : tableView.getColumns()) {
      ColumnHeader header = (ColumnHeader) column.getStyleableNode();
      header.resizeColumnToFitContent();
    }
  }

}
