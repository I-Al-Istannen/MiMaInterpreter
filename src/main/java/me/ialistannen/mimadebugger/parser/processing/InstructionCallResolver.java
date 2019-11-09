package me.ialistannen.mimadebugger.parser.processing;

import java.util.List;
import me.ialistannen.mimadebugger.exceptions.AssemblyInstructionNotFoundException;
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
   * @throws AssemblyInstructionNotFoundException if an instruction could not be found
   * @throws MiMaSyntaxError if another syntax error occurs
   */
  public void resolveInstructions(SyntaxTreeNode root, InstructionSet instructionSet)
      throws MiMaSyntaxError {
    root.accept(new InstructionCallResolveVisitor(instructionSet));
  }

  private static class InstructionCallResolveVisitor implements NodeVisitor {

    private final InstructionSet instructionSet;

    InstructionCallResolveVisitor(InstructionSet instructionSet) {
      this.instructionSet = instructionSet;
    }

    @Override
    public void visit(SyntaxTreeNode node) throws MiMaSyntaxError {
      NodeVisitor.super.visit(node);
    }

    @Override
    public void visitInstructionNode(InstructionNode instructionNode) throws MiMaSyntaxError {
      Instruction instruction = instructionSet
          .forName(instructionNode.getInstruction())
          .orElseThrow(() -> new AssemblyInstructionNotFoundException(
              instructionNode.getInstruction(),
              instructionNode.getAddress(),
              instructionNode.getStringReader()
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

      InstructionCall instructionCall = ImmutableInstructionCall.builder()
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
