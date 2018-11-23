package me.ialistannen.mimadebugger.gui.highlighting;

public enum HighlightingCategory {
  INSTRUCTION("highlight-instruction"),
  VALUE("highlight-value"),
  BINARY("highlight-binary");

  private String cssClass;

  HighlightingCategory(String cssClass) {
    this.cssClass = cssClass;
  }

  /**
   * The CSS class for this category.
   *
   * @return the CSS class for this category
   */
  public String getCssClass() {
    return cssClass;
  }
}
