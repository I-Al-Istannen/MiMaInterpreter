package me.ialistannen.mimadebugger.gui.execution;

import java.util.Set;
import java.util.function.Supplier;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.NamedExecutionError;
import me.ialistannen.mimadebugger.machine.MiMaRunner;

/**
 * Only tries a limited amount of times before it considers the execution temporarily failed.
 */
class LimitedStepsExecutionStrategy extends ExecutionStrategy {

  private int maximumStepCount;

  LimitedStepsExecutionStrategy(int maximumStepCount) {
    this.maximumStepCount = maximumStepCount;
  }

  @Override
  public void run(MiMaRunner runner, Set<Integer> breakpoints, Supplier<Boolean> cancelledSupplier)
      throws MiMaException {
    for (int i = 0; i < maximumStepCount && !cancelledSupplier.get(); i++) {
      if (!singleStep(runner, breakpoints).isPresent()) {
        return;
      }
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
