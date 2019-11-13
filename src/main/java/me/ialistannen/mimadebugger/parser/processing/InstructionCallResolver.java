package me.ialistannen.mimadebugger.parser.processing;

import java.util.List;
import java.util.Optional;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.parser.ast.InstructionCallNode;
import me.ialistannen.mimadebugger.parser.ast.InstructionNode;
import me.ialistannen.mimadebugger.parser.ast.LiteralValueNode;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;
import me.ialistannen.mimadebugger.parser.validation.ImmutableParsingProblem;

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
    public void visitInstructionNode(InstructionNode instructionNode) {
      if (instructionNode.hasProblem()) {
        return;
      }
      Instruction instruction = instructionSet
          .forName(instructionNode.getInstruction())
          .orElseThrow(() -> new RuntimeException("Instruction unknown :("));

      List<SyntaxTreeNode> children = instructionNode.getChildren();

      if (instruction.hasArgument() && children.isEmpty()) {
        instructionNode.addProblem(ImmutableParsingProblem.builder()
            .message(String.format("Expected argument for instruction %s", instruction.name()))
            .approximateSpan(instructionNode.getSpan())
            .build()
        );
        return;
      }

      int argument = 0;
      if (!children.isEmpty()) {
        SyntaxTreeNode node = children.get(0);
        if (!(node instanceof LiteralValueNode)) {
          instructionNode.addProblem(ImmutableParsingProblem.builder()
              .approximateSpan(instructionNode.getSpan())
              .message("Expected a literal value, got " + node)
              .build()
          );
          return;
        }

        argument = ((LiteralValueNode) node).getValue();
      }

      InstructionCall instructionCall = ImmutableInstructionCall.builder()
          .argument(argument)
          .command(instruction)
          .build();

      Optional<SyntaxTreeNode> parent = instructionNode.getParent();

      if (!parent.isPresent()) {
        instructionNode.addProblem(ImmutableParsingProblem.builder()
            .message("Dangling instruction node: " + instructionNode)
            .approximateSpan(instructionNode.getSpan())
            .build()
        );
        return;
      }

      parent.get().removeChild(instructionNode);
      InstructionCallNode callNode = new InstructionCallNode(
          instructionNode.getAddress(),
          instructionNode.getStringReader(),
          instructionCall,
          instructionNode.getSpan()
      );
      callNode.addChild(instructionNode);
      parent.get().addChild(callNode);
    }
  }
}
