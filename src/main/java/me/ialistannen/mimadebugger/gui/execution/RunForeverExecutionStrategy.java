package me.ialistannen.mimadebugger.gui.execution;

import java.util.Set;
import java.util.function.Supplier;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.MiMaRunner;

/**
 * Runs for as long as the program did not halt.
 */
class RunForeverExecutionStrategy extends ExecutionStrategy {

  @Override
  void run(MiMaRunner runner, Set<Integer> breakpoints, Supplier<Boolean> cancelledSupplier)
      throws MiMaException {
    while (!cancelledSupplier.get()) {
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
