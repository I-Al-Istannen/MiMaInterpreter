package me.ialistannen.mimadebugger.parser.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.ialistannen.mimadebugger.gui.state.ImmutableEncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionCallNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelUsageNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.validation.ImmutableParsingProblem;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class ToMemoryValueConverter {

  /**
   * Processes the given tree and returns all found {@link MemoryValue}s.
   *
   * @param node the node to start with
   * @return all found memory values. Empty if there was an error, query {@link
   *     SyntaxTreeNode#getAllParsingProblems()} to get it
   */
  public Optional<List<MemoryValue>> process(SyntaxTreeNode node) {
    List<MemoryValue> values = new ArrayList<>();

    node.accept(new ToMemoryValueConversionVisitor(values));

    if (node.hasProblem()) {
      return Optional.empty();
    }

    return Optional.of(values);
  }

  private static class ToMemoryValueConversionVisitor implements NodeVisitor {

    private final List<MemoryValue> values;

    ToMemoryValueConversionVisitor(List<MemoryValue> values) {
      this.values = values;
    }

    @Override
    public void visitInstructionCallNode(InstructionCallNode instructionCallNode) {
      InstructionCall call = instructionCallNode.getInstructionCall();
      values.add(
          ImmutableEncodedInstructionCall.builder()
              .instructionCall(call)
              .address(instructionCallNode.getAddress())
              .representation(MemoryFormat.combineInstruction(call))
              .build()
      );
    }

    @Override
    public void visitInstructionNode(InstructionNode instructionNode) {
      instructionNode.addProblem(ImmutableParsingProblem.builder()
          .message("Instruction node left over after parsing: " + instructionNode)
          .approximateSpan(instructionNode.getSpan())
          .build()
      );
    }

    @Override
    public void visitLabelUsageNode(LabelUsageNode labelNode) {
      if (labelNode.getReferencedAddress() >= 0) {
        return;
      }
      labelNode.addProblem(ImmutableParsingProblem.builder()
          .message("Label left over after parsing: " + labelNode)
          .approximateSpan(labelNode.getSpan())
          .build()
      );
    }

    @Override
    public void visitConstantNode(ConstantNode constantNode) {
      values.add(
          ImmutableEncodedInstructionCall
              .constantValue(constantNode.getValue(), constantNode.getAddress())
      );
      visitChildren(constantNode);
    }
  }
}
