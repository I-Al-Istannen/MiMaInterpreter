package me.ialistannen.mimadebugger.parser.processing;

import java.util.ArrayList;
import java.util.List;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.gui.state.ImmutableEncodedInstructionCall;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionCallNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LabelNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class ToMemoryValueConverter {

  /**
   * Processes the given tree and returns all found {@link MemoryValue}s.
   *
   * @param node the node to start with
   * @return all found memory values
   */
  public List<MemoryValue> process(SyntaxTreeNode node) throws MiMaSyntaxError {
    List<MemoryValue> values = new ArrayList<>();

    node.accept(new ToMemoryValueConversionVisitor(values));

    return values;
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
    public void visitInstructionNode(InstructionNode instructionNode) throws MiMaSyntaxError {
      throw new MiMaSyntaxError(
          "Instruction call left over after parsing!" + instructionNode,
          instructionNode.getStringReader()
      );
    }

    @Override
    public void visitLabelNode(LabelNode labelNode) throws MiMaSyntaxError {
      throw new MiMaSyntaxError(
          "Label left over after parsing!" + labelNode, labelNode.getStringReader()
      );
    }

    @Override
    public void visitConstantNode(ConstantNode constantNode) {
      values.add(
          ImmutableEncodedInstructionCall
              .constantValue(constantNode.getValue(), constantNode.getAddress())
      );
    }
  }
}
