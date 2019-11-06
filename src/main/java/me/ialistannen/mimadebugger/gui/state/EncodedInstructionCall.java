package me.ialistannen.mimadebugger.gui.state;

import org.immutables.value.Value;

@Value.Immutable
public abstract class EncodedInstructionCall implements MemoryValue {

  /**
   * Represents a constant memory value.
   *
   * @param value the value
   * @param address the address
   * @return a memory value with the given constant value
   */
  public static EncodedInstructionCall constantValue(int value, int address) {
    return ImmutableEncodedInstructionCall.builder()
        .address(address)
        .representation(value)
        .build();
  }

}
