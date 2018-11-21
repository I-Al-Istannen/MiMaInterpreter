package me.ialistannen.mimadebugger.exceptions;

public class InstructionNotFoundException extends MiMaException {

  public InstructionNotFoundException(String name) {
    super("Instruction '" + name + "' not found!");
  }

  public InstructionNotFoundException(int opcode) {
    this(Integer.toString(opcode));
  }
}
