package me.ialistannen.mimadebugger.gui.execution;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.State;

/**
 * An execution strategy that simulates a cpu clock and executes instructions at a given clock
 * cycle.
 */
class ClockExecutionStrategy extends ExecutionStrategy {

  private long msPerTick;

  /**
   * Creates a new clock execution strategy.
   *
   * @param msPerTick the time per cycle in milliseconds
   */
  ClockExecutionStrategy(long msPerTick) {
    this.msPerTick = msPerTick;
  }

  @Override
  void run(MiMaRunner runner, Set<Integer> breakpoints, Supplier<Boolean> cancelledSupplier,
      Consumer<State> uiUpdater) throws MiMaException {

    try {
      step(runner, breakpoints, cancelledSupplier, uiUpdater);
    } catch (InterruptedException e) {
      throw new MiMaException("Interrupted :(");
    }
  }

  private void step(MiMaRunner runner, Set<Integer> breakpoints,
      Supplier<Boolean> cancelledSupplier, Consumer<State> uiUpdater)
      throws InterruptedException, MiMaException {
    long nextTick = System.currentTimeMillis();

    while (!cancelledSupplier.get()) {

      long timeToNextTick = nextTick - System.currentTimeMillis();
      if (timeToNextTick > 250) {
        Thread.sleep(timeToNextTick - 250, 0);
      } else if (timeToNextTick < 0) {
        long ticksToSkip = (long) Math.abs(Math.ceil((float) timeToNextTick / msPerTick));
        ticksToSkip += 2; // some leeway

        System.out.println("Can't keep up! Skipping " + ticksToSkip + " ticks");
        nextTick += msPerTick * ticksToSkip;
        continue;
      }

      //noinspection StatementWithEmptyBody
      while (System.currentTimeMillis() < nextTick) {
        // Spin wait loop
      }

      Optional<State> step = singleStep(runner, breakpoints);
      if (!step.isPresent()) {
        return;
      }
      uiUpdater.accept(step.get());

      nextTick = nextTick + msPerTick;
    }
  }

  @Override
  public String toString() {
    return "Clock cycles (" + msPerTick + "ms)";
  }
}
