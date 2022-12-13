package me.ialistannen.mimadebugger.gui.util;

import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;

public class AutoSizableTableViewSkin<T> extends TableViewSkin<T> {

  public AutoSizableTableViewSkin(TableView<T> tableView) {
    super(tableView);
  }

  @Override
  protected TableHeaderRow createTableHeaderRow() {
    return new MyTableHeaderRow(this);
  }

  public static class MyTableHeaderRow extends TableHeaderRow {

    public MyTableHeaderRow(TableViewSkinBase base) {
      super(base);
    }

    @Override
    protected NestedTableColumnHeader createRootHeader() {
      return new MyNestedTableColumnHeader();
    }

  }

  public static class ColumnHeader extends TableColumnHeader {

    public ColumnHeader(TableColumnBase col) {
      super(col);
    }

    public void resizeColumnToFitContent() {
      super.resizeColumnToFitContent(-1);
    }
  }

  public static class MyNestedTableColumnHeader extends NestedTableColumnHeader {

    public MyNestedTableColumnHeader() {
      super(null);
    }

    @Override
    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
      return new ColumnHeader(col);
    }

  }
}
