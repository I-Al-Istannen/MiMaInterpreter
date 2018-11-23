package me.ialistannen.mimadebugger.gui.state;

import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import org.immutables.value.Value;

@Value.Immutable
public abstract class EncodedInstructionCall {

  /**
   * Returns the {@link InstructionCall} that is encoded.
   *
   * @return the {@link InstructionCall} that is encoded
   */
  public abstract InstructionCall instructionCall();

  /**
   * Returns the encoded representation of this call.
   *
   * @return the encoded representation of this call
   */
  public abstract String representation();

  /**
   * Returns thr address in the {@link MainMemory} that the call is stored in.
   *
   * @return thr address in the {@link MainMemory} that the call is stored in
   */
  public abstract int address();
}
