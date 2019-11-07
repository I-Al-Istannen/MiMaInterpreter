package me.ialistannen.mimadebugger.gui.execution;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.State;

abstract class ExecutionStrategy {

  /**
   * Runs the program, reporting errors.
   *
   * @param runner the runner to use
   * @param breakpoints the breakpoints to use
   * @param cancelledSupplier defines whether this execution was cancelled
   */
  abstract void run(MiMaRunner runner, Set<Integer> breakpoints,
      Supplier<Boolean> cancelledSupplier) throws MiMaException;

  /**
   * Performs a single step or stops the execution.
   *
   * @param runner the runner to use
   * @param breakpoints the breakpoints
   * @return the current step or en empty optional if it should terminate
   */
  protected Optional<State> singleStep(MiMaRunner runner, Set<Integer> breakpoints) {
    State step = runner.nextStep();

    if (step.registers().accumulator() == 0xFFFF) {
      return Optional.empty();
    }

    if (breakpoints.contains(step.registers().instructionPointer())) {
      return Optional.empty();
    }

    return Optional.of(step);
  }
}
