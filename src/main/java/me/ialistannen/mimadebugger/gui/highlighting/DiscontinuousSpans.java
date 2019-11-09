package me.ialistannen.mimadebugger.gui.highlighting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import me.ialistannen.mimadebugger.util.ClosedIntRange;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class DiscontinuousSpans {

  private SortedSet<StyleSpan> spans;

  public DiscontinuousSpans() {
    this.spans = new TreeSet<>();
  }

  /**
   * Adds a new span.
   *
   * @param cssClasses the css classes for it.
   * @param range the range to add it at
   * @throws IllegalArgumentException if the range overlaps another one
   */
  public void addSpan(List<String> cssClasses, ClosedIntRange range) {
    Optional<StyleSpan> overlapping = findOverlapping(range);

    if (overlapping.isPresent()) {
      throw new IllegalArgumentException(
          "Overlapping range found. " + cssClasses + "/" + range
              + " collides with " + overlapping.get()
      );
    }
    this.spans.add(new StyleSpan(cssClasses, range));
  }

  /**
   * Checks whether this span has no entries.
   *
   * @return true if this span has no entries
   */
  public boolean isEmpty() {
    return spans.isEmpty();
  }

  /**
   * Converts this discontinuous span to a continuous one.
   *
   * @return a continuous span
   */
  public StyleSpans<Collection<String>> toStyleSpans() {
    StyleSpansBuilder<Collection<String>> styleSpansBuilder = new StyleSpansBuilder<>();

    int lastEnd = 0;

    for (StyleSpan span : spans) {
      if (span.range.getStart() > lastEnd + 1) {
        styleSpansBuilder.add(Collections.emptyList(), span.range.getStart() - lastEnd - 1);
      }

      styleSpansBuilder.add(span.cssClass, span.range.getLength());

      lastEnd = span.range.getEnd();
    }

    return styleSpansBuilder.create();
  }

  private Optional<StyleSpan> findOverlapping(ClosedIntRange range) {
    for (StyleSpan span : spans) {
      if (span.range.intersects(range)) {
        return Optional.of(span);
      }
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    return "DiscontinuousSpans{" +
        "spans=" + spans +
        '}';
  }

  private static class StyleSpan implements Comparable<StyleSpan> {

    private List<String> cssClass;
    private ClosedIntRange range;

    private StyleSpan(List<String> cssClasses, ClosedIntRange range) {
      this.cssClass = new ArrayList<>(cssClasses);
      this.range = range;
    }

    @Override
    public int compareTo(StyleSpan o) {
      return Integer.compare(range.getStart(), o.range.getStart());
    }

    @Override
    public String toString() {
      return cssClass + "/" + range;
    }
  }
}
