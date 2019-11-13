package me.ialistannen.mimadebugger.gui.text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.RootNode;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.util.MutableStringReader;
import me.ialistannen.mimadebugger.util.HalfOpenIntRange;
import org.fxmisc.richtext.CodeArea;

public class LineGutterWithBreakpoint implements IntFunction<Node> {

  private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
  private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
  private static final Font DEFAULT_FONT =
      Font.font("monospace", FontPosture.ITALIC, 13);
  private static final Background DEFAULT_BACKGROUND =
      new Background(new BackgroundFill(Color.web("#ddd"), null, null));

  private Set<Integer> breakpoints;
  private int gutterLineWidth;
  private ObservableValue<SyntaxTreeNode> ast;

  private IntConsumer breakpointToggleListener;

  public LineGutterWithBreakpoint(CodeArea codeArea, Set<Integer> breakpoints,
      ObservableValue<SyntaxTreeNode> ast,
      IntConsumer breakpointToggleListener) {
    this.breakpoints = new HashSet<>(breakpoints);
    this.ast = ast;
    this.breakpointToggleListener = breakpointToggleListener;

    int lineCount = codeArea.getParagraphs().size();
    if (ast.getValue() != null) {
      CollectingNodeVisitor visitor = new CollectingNodeVisitor();
      visitor.visit(ast.getValue());
      lineCount = visitor.getDepths().stream()
          .map(NodeWithDepth::getNode)
          .mapToInt(SyntaxTreeNode::getAddress)
          .max()
          .orElse(lineCount);
    }

    // calculate maximum size of the line number label to provide a constant width gutter
    this.gutterLineWidth = (int) Math.ceil(
        new Text(Integer.toString(lineCount))
            .getLayoutBounds().getWidth()
    );
  }

  @Override
  public Node apply(int storedLineNumber) {
    HBox container = createContainer();

    Optional<SyntaxTreeNode> nodeAtLine = findNodeAtLine(storedLineNumber);
    int lineNumber = nodeAtLine.map(SyntaxTreeNode::getAddress).orElse(-1);
    Label lineNumberLabel = createLineNumberLabel(lineNumber);

    Circle breakpointIndicator = createBreakpointIndicator(container);
    updateIndicatorFill(breakpointIndicator, lineNumber);

    container.getChildren().add(lineNumberLabel);
    container.getChildren().add(breakpointIndicator);

    boolean sameAsBefore = findNodeAtLine(storedLineNumber - 1)
        .map(SyntaxTreeNode::getAddress)
        .map(it -> it.equals(lineNumber))
        .orElse(false);

    if (lineNumber >= 0) {
      container.setOnMouseClicked(event -> breakpointToggleListener.accept(lineNumber));
      updateIndicatorFill(breakpointIndicator, lineNumber);
    }
    if (sameAsBefore || lineNumber < 0) {
      lineNumberLabel.setText("");
    }
    return container;
  }

  private Circle createBreakpointIndicator(HBox container) {
    Circle breakpointIndicator = new Circle(4);
    breakpointIndicator.translateYProperty()
        .bind(
            container.heightProperty()
                .divide(2)
                .subtract(breakpointIndicator.getRadius())
        );
    return breakpointIndicator;
  }

  private HBox createContainer() {
    HBox container = new HBox();
    container.setFillHeight(true);
    container.setSpacing(5);
    container.setBackground(DEFAULT_BACKGROUND);
    container.setPadding(DEFAULT_INSETS);
    container.setMaxWidth(Double.MAX_VALUE);

    container.setCursor(Cursor.HAND);
    return container;
  }

  private Label createLineNumberLabel(int lineNumber) {
    Label lineNumberLabel = new Label(Integer.toString(lineNumber));
    lineNumberLabel.setFont(DEFAULT_FONT);
    lineNumberLabel.setTextFill(DEFAULT_TEXT_FILL);
    lineNumberLabel.setAlignment(Pos.TOP_RIGHT);
    lineNumberLabel.getStyleClass().add("lineno");
    lineNumberLabel.setPrefWidth(gutterLineWidth);
    lineNumberLabel.setMaxWidth(gutterLineWidth);
    return lineNumberLabel;
  }

  private void updateIndicatorFill(Circle circle, int line) {
    if (breakpoints.contains(line)) {
      circle.setFill(Color.ROYALBLUE);
    } else {
      circle.setFill(Color.TRANSPARENT);
    }
  }

  private Optional<SyntaxTreeNode> findNodeAtLine(int line) {
    if (ast.getValue() == null || line < 0) {
      return Optional.empty();
    }
    MutableStringReader reader = new MutableStringReader(
        ast.getValue().getStringReader().getString()
    );
    for (int i = 0; i < line; i++) {
      reader.read(Pattern.compile("[^\\n]*(\\n)?"));
    }
    int lineStart = reader.getCursor();
    reader.read(Pattern.compile("[^\\n]*(\\n)?"));
    int lineEnd = reader.getCursor();
    HalfOpenIntRange lineSpan = new HalfOpenIntRange(lineStart, lineEnd);

    CollectingNodeVisitor collectingNodeVisitor = new CollectingNodeVisitor();
    collectingNodeVisitor.visit(ast.getValue());

    return collectingNodeVisitor.getDepths().stream()
        .filter(nodeWithDepth -> nodeWithDepth.getNode().getSpan().intersects(lineSpan))
        .filter(node -> !(node.getNode() instanceof RootNode))
        .max(
            Comparator.comparing(nodeWithDepth -> nodeWithDepth.getNode().getAddress())
        )
        .map(NodeWithDepth::getNode);
  }

  private static class CollectingNodeVisitor implements NodeVisitor {

    private List<NodeWithDepth> depths = new ArrayList<>();

    @Override
    public void visit(SyntaxTreeNode node) {
      visit(node, 0);
    }

    private void visit(SyntaxTreeNode node, int depth) {
      depths.add(new NodeWithDepth(node, depth));

      node.getChildren().forEach(child -> visit(child, depth + 1));
    }

    List<NodeWithDepth> getDepths() {
      return depths;
    }
  }

  private static class NodeWithDepth {

    private SyntaxTreeNode node;
    private int depth;

    NodeWithDepth(SyntaxTreeNode node, int depth) {
      this.node = node;
      this.depth = depth;
    }

    SyntaxTreeNode getNode() {
      return node;
    }

    int getDepth() {
      return depth;
    }

    @Override
    public String toString() {
      return "NodeWithDepth{" +
          "node=" + node +
          ", depth=" + depth +
          '}';
    }
  }

}
