package me.ialistannen.mimadebugger.gui.highlighting;

public enum HighlightingCategory {
  INSTRUCTION("highlight-instruction"),
  VALUE("highlight-value"),
  BINARY("highlight-binary"),
  COMMENT("highlight-comment"),
  LABEL_DECLARATION("highlight-label-declaration"),
  LABEL_USAGE("highlight-label-usage"),
  NORMAL("highlight-normal");

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
