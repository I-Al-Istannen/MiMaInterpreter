package me.ialistannen.mimadebugger.exceptions;

public class InstructionNotFoundException extends MiMaException {

  public InstructionNotFoundException(String name) {
    super("Instruction '" + name + "' not found!");
  }

  public InstructionNotFoundException(String name, int line) {
    super(String.format("Instruction '%s' not found at line %d!", name, line));
  }

  public InstructionNotFoundException(int opcode) {
    this(Integer.toString(opcode));
  }
}
