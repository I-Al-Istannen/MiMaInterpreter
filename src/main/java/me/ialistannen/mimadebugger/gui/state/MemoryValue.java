package me.ialistannen.mimadebugger.gui.state;

import java.util.Optional;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;

public interface MemoryValue {

  /**
   * Returns the {@link InstructionCall} that is encoded.
   *
   * @return the {@link InstructionCall} that is encoded, if any
   */
  Optional<InstructionCall> instructionCall();

  /**
   * Returns the encoded representation of this value.
   *
   * @return the encoded representation of this value
   */
  String representation();

  /**
   * Returns the address in the {@link MainMemory} that the value is stored in.
   *
   * @return the address in the {@link MainMemory} that the value is stored in
   */
  int address();
}
