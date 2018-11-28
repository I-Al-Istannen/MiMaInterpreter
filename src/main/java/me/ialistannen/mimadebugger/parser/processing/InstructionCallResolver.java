package me.ialistannen.mimadebugger.parser.processing;

import java.util.List;
import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.ast.ConstantNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionCallNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;

public class InstructionCallResolver {

  /**
   * Resolves the {@link Instruction}s to {@link InstructionCall}s.
   *
   * @param root the root node to walk through
   * @param instructionSet the {@link InstructionSet} to use
   */
  public void resolveInstructions(SyntaxTreeNode root, InstructionSet instructionSet) {
    root.accept(new InstructionCallResolveVisitor(instructionSet));
  }

  private static class InstructionCallResolveVisitor implements NodeVisitor {

    private final InstructionSet instructionSet;

    InstructionCallResolveVisitor(InstructionSet instructionSet) {
      this.instructionSet = instructionSet;
    }

    @Override
    public void visit(SyntaxTreeNode node) {
      NodeVisitor.super.visit(node);
    }

    @Override
    public void visitInstructionNode(InstructionNode instructionNode) {
      Instruction instruction = instructionSet
          .forName(instructionNode.getInstruction())
          .orElseThrow(() -> new InstructionNotFoundException(
              instructionNode.getInstruction(), instructionNode.getAddress()
          ));

      List<SyntaxTreeNode> children = instructionNode.getChildren();

      if (instruction.hasArgument() && children.isEmpty()) {
        throw new MiMaSyntaxError(
            String.format("Expected argument for instruction %s", instruction.name()),
            instructionNode.getStringReader()
        );
      }

      int argument = 0;
      if (!children.isEmpty()) {
        SyntaxTreeNode node = children.get(0);
        if (!(node instanceof ConstantNode)) {
          throw new MiMaSyntaxError(
              String.format("Expected constant number for instruction '%s'", instruction.name()),
              instructionNode.getStringReader()
          );
        }

        argument = ((ConstantNode) node).getValue();
        instructionNode.removeChild(node);
      }

      ImmutableInstructionCall instructionCall = ImmutableInstructionCall.builder()
          .argument(argument)
          .command(instruction)
          .build();

      SyntaxTreeNode parent = instructionNode.getParent()
          .orElseThrow(() -> new MiMaSyntaxError(
                  "Dangling instruction node: " + instructionNode,
                  instructionNode.getStringReader()
              )
          );

      parent.removeChild(instructionNode);
      parent.addChild(new InstructionCallNode(
          instructionNode.getAddress(), instructionNode.getStringReader(), instructionCall)
      );
    }
  }
}
