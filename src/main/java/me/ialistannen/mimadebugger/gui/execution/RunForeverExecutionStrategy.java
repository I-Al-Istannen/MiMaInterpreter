package me.ialistannen.mimadebugger.gui.execution;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.State;

/**
 * Runs for as long as the program did not halt.
 */
class RunForeverExecutionStrategy extends ExecutionStrategy {

  @Override
  void run(MiMaRunner runner, Set<Integer> breakpoints, Supplier<Boolean> cancelledSupplier,
      Consumer<State> uiUpdater)
      throws MiMaException {
    while (!cancelledSupplier.get()) {
      Optional<State> step = singleStep(runner, breakpoints);
      if (!step.isPresent()) {
        return;
      }
      uiUpdater.accept(step.get());
    }
  }

  @Override
  public String toString() {
    return "Run until HALT";
  }
}
