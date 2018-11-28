package me.ialistannen.mimadebugger.parser.processing;

import java.util.HashMap;
import java.util.Map;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.LabelNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;

public class LabelResolver {

  /**
   * Resolves all labels and replaces them with the address they point to or nothing, if they are a
   * declaration.
   *
   * @param syntaxTreeNode the root node to resolve all labels for
   */
  public void resolve(SyntaxTreeNode syntaxTreeNode) {
    LabelCollectionVisitor visitor = new LabelCollectionVisitor();
    syntaxTreeNode.accept(visitor);

    Map<String, Integer> labels = visitor.getLabels();
    syntaxTreeNode.accept(new LabelReplacementVisitor(labels));
  }

  /**
   * In the first pass all label addresses are gathered.
   */
  private static class LabelCollectionVisitor implements NodeVisitor {

    private Map<String, Integer> labels = new HashMap<>();

    @Override
    public void visitLabelNode(LabelNode labelNode) {
      if (labelNode.isDeclaration()) {
        labels.put(labelNode.getName(), labelNode.getAddress());
      }
    }

    Map<String, Integer> getLabels() {
      return labels;
    }
  }

  /**
   * In a second pass all labels are replaced using the knowledge gained in the first pass.
   */
  private static class LabelReplacementVisitor implements NodeVisitor {

    private Map<String, Integer> labels;

    private LabelReplacementVisitor(Map<String, Integer> labels) {
      this.labels = labels;
    }

    @Override
    public void visitLabelNode(LabelNode labelNode) {
      if (labelNode.isDeclaration()) {
        labelNode.getParent().ifPresent(node -> {
          node.removeChild(labelNode);
          labelNode.getChildren().forEach(node::addChild);
        });
      } else {
        if (!labels.containsKey(labelNode.getName())) {
          throw new MiMaSyntaxError(
              "Label unknown", labelNode.getStringReader()
          );
        }
        labelNode.getParent().ifPresent(node -> {
          node.removeChild(labelNode);
          node.addChild(new ConstantNode(
              labels.get(labelNode.getName()), labelNode.getAddress(), labelNode.getStringReader()
          ));
        });
      }
    }

  }
}
