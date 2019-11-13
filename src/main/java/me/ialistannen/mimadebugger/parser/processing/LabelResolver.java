package me.ialistannen.mimadebugger.parser.processing;

import java.util.HashMap;
import java.util.Map;
import me.ialistannen.mimadebugger.parser.ast.LabelDeclarationNode;
import me.ialistannen.mimadebugger.parser.ast.LabelUsageNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.validation.ImmutableParsingProblem;

public class LabelResolver {

  /**
   * Resolves all labels and sets their referenced address.
   *
   * @param syntaxTreeNode the root node to resolve all labels for
   */
  public void resolve(SyntaxTreeNode syntaxTreeNode) {
    LabelCollectionVisitor visitor = new LabelCollectionVisitor();
    syntaxTreeNode.accept(visitor);

    Map<String, Integer> labels = visitor.getLabels();
    syntaxTreeNode.accept(new LabelResolveVisitor(labels));
  }

  /**
   * In the first pass all label addresses are gathered.
   */
  private static class LabelCollectionVisitor implements NodeVisitor {

    private Map<String, Integer> labels = new HashMap<>();

    @Override
    public void visitLabelDeclarationNode(LabelDeclarationNode labelNode) {
      if (labels.containsKey(labelNode.getName())) {
        labelNode.addProblem(ImmutableParsingProblem.builder()
            .message("Duplicated label '" + labelNode.getName() + "'")
            .approximateSpan(labelNode.getSpan())
            .build()
        );
      }
      labels.putIfAbsent(labelNode.getName(), labelNode.getAddress());

      visitChildren(labelNode);
    }

    Map<String, Integer> getLabels() {
      return labels;
    }
  }

  /**
   * In a second pass all labels are replaced using the knowledge gained in the first pass.
   */
  private static class LabelResolveVisitor implements NodeVisitor {

    private Map<String, Integer> labels;

    private LabelResolveVisitor(Map<String, Integer> labels) {
      this.labels = labels;
    }

    @Override
    public void visitLabelUsageNode(LabelUsageNode labelNode) {
      if (!labels.containsKey(labelNode.getName())) {
        labelNode.addProblem(ImmutableParsingProblem.builder()
            .message("Label unknown")
            .approximateSpan(labelNode.getSpan())
            .build()
        );
        return;
      }
      labelNode.setReferencedAddress(labels.get(labelNode.getName()));

      visitChildren(labelNode);
    }
  }
}
