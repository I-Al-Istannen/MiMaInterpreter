package me.ialistannen.mimadebugger.gui.state;

import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import org.immutables.value.Value;

@Value.Immutable
public abstract class EncodedInstructionCall {

  public abstract InstructionCall instructionCall();

  public abstract String representation();
}
