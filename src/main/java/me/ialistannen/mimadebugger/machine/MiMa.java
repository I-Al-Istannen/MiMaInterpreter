package me.ialistannen.mimadebugger.machine;

import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;

public class MiMa {

  private State currentState;
  private InstructionSet instructionSet;

  /**
   * Creates a new MiMa.
   *
   * @param initialState the initial state
   * @param instructionSet the {@link InstructionSet} to use.
   */
  public MiMa(State initialState, InstructionSet instructionSet) {
    this.currentState = initialState;
    this.instructionSet = instructionSet;
  }

  /**
   * Performs a single calculation step.
   *
   * @return the new state
   */
  public State step() {
    currentState = fetchNextInstruction();
    int currentInstruction = currentState.registers().instruction();

    InstructionCall instructionCall = instructionSet.forEncodedValue(currentInstruction)
        .orElseThrow(() -> new InstructionNotFoundException(currentInstruction));

    if (instructionCall.command().name().equalsIgnoreCase("HALT")) {
      throw new ProgramHaltException();
    }

    // Increment IP
    currentState = fetchNextInstructionPointer();

    // Execute the current instruction (after incrementing the IP, so jumps work correctly)
    currentState = instructionCall.command().apply(currentState, instructionCall.argument());

    return currentState;
  }

  private State fetchNextInstructionPointer() {
    return currentState.copy()
        .withRegisters(
            currentState.registers().copy()
                .withInstruction(
                    currentState.memory().get(currentState.registers().instructionPointer())
                )
                .withInstructionPointer(currentState.registers().instructionPointer() + 1)
                // Reset ALU
                .withAluInputLeft(0)
                .withAluInputRight(0)
        );
  }

  private State fetchNextInstruction() {
    return currentState.copy()
        .withRegisters(
            currentState.registers().copy()
                .withInstruction(
                    currentState.memory().get(currentState.registers().instructionPointer())
                )
        );
  }

  /**
   * Returns the current {@link State} of the MiMa.
   *
   * @return the current {@link State} of the MiMa
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Returns a copy of this mima in the given state.
   *
   * @param newState the state of the copy
   * @return the new mima
   */
  public MiMa copy(State newState) {
    return new MiMa(newState, instructionSet);
  }
}