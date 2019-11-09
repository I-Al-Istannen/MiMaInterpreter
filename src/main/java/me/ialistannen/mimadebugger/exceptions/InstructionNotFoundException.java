package me.ialistannen.mimadebugger.exceptions;

public class InstructionNotFoundException extends MiMaException {

  public InstructionNotFoundException(int opcode) {
    super(String.format("Instruction '%d'(%s) not found!", opcode, Integer.toHexString(opcode)));
  }
}
