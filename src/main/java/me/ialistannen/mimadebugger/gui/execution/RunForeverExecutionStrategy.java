package me.ialistannen.mimadebugger.gui.execution;

import java.util.Set;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.MiMaRunner;

/**
 * Runs for as long as the program did not halt.
 */
class RunForeverExecutionStrategy extends ExecutionStrategy {

  @Override
  void run(MiMaRunner runner, Set<Integer> breakpoints) throws MiMaException {
    while (true) {
      if (!singleStep(runner, breakpoints).isPresent()) {
        return;
      }
    }
  }

  @Override
  public String toString() {
    return "Run until HALT";
  }
}
