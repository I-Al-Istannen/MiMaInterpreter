package me.ialistannen.mimadebugger.gui.state;

import java.util.Optional;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import org.immutables.value.Value;

@Value.Immutable
public abstract class EncodedInstructionCall implements MemoryValue {

  /**
   * Returns the {@link InstructionCall} that is encoded.
   *
   * @return the {@link InstructionCall} that is encoded, if any
   */
  @Override
  public abstract Optional<InstructionCall> instructionCall();

  /**
   * Returns the encoded representation of this call.
   *
   * @return the encoded representation of this call
   */
  @Override
  public abstract String representation();

  /**
   * Returns thr address in the {@link MainMemory} that the call is stored in.
   *
   * @return thr address in the {@link MainMemory} that the call is stored in
   */
  @Override
  public abstract int address();
}
