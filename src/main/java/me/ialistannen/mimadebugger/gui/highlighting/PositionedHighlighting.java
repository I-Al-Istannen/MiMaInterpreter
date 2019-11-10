package me.ialistannen.mimadebugger.gui.highlighting;

import java.util.Collection;
import org.fxmisc.richtext.model.StyleSpans;

/**
 * A simple highlighting span associated with a position.
 */
public class PositionedHighlighting {

  private StyleSpans<Collection<String>> styles;
  private int start;

  public PositionedHighlighting(StyleSpans<Collection<String>> styles, int start) {
    this.styles = styles;
    this.start = start;
  }

  public StyleSpans<Collection<String>> getStyles() {
    return styles;
  }

  public int getStart() {
    return start;
  }
}
