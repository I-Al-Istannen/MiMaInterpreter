package me.ialistannen.mimadebugger.gui.execution;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.NamedExecutionError;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.State;

/**
 * Only tries a limited amount of times before it considers the execution temporarily failed.
 */
class LimitedStepsExecutionStrategy extends ExecutionStrategy {

  private int maximumStepCount;

  LimitedStepsExecutionStrategy(int maximumStepCount) {
    this.maximumStepCount = maximumStepCount;
  }

  @Override
  public void run(MiMaRunner runner, Set<Integer> breakpoints, Supplier<Boolean> cancelledSupplier,
      Consumer<State> uiUpdater)
      throws MiMaException {
    for (int i = 0; i < maximumStepCount && !cancelledSupplier.get(); i++) {
      Optional<State> state = singleStep(runner, breakpoints);
      if (!state.isPresent()) {
        return;
      }
      uiUpdater.accept(state.get());
    }
    throw new NamedExecutionError(
        "Execution exceeded " + maximumStepCount + " steps!",
        "Execution time exceeded"
    );
  }

  @Override
  public String toString() {
    return "Limit step count";
  }
}
