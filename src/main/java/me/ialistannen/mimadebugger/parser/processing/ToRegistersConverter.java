package me.ialistannen.mimadebugger.parser.processing;

import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.Registers;
import me.ialistannen.mimadebugger.parser.ast.AssemblerDirectiveRegister;
import me.ialistannen.mimadebugger.parser.ast.NodeVisitor;
import me.ialistannen.mimadebugger.parser.ast.SyntaxTreeNode;

/**
 * Converts an ast to a register value dump.
 */
public class ToRegistersConverter {

  /**
   * Converts the AST to a register dump.
   *
   * @param root the root
   * @return the final registers
   */
  public Registers toRegisters(SyntaxTreeNode root) {
    ExtractingNodeVisitor nodeVisitor = new ExtractingNodeVisitor();
    root.accept(nodeVisitor);

    return nodeVisitor.registers;
  }

  private static class ExtractingNodeVisitor implements NodeVisitor {

    Registers registers = ImmutableRegisters.builder().build();

    @Override
    public void visitAssemblerDirectiveRegister(AssemblerDirectiveRegister node) {
      node.getValue()
          .ifPresent(value -> registers = node.getRegister().set(registers, value.getValue()));
    }
  }
}
