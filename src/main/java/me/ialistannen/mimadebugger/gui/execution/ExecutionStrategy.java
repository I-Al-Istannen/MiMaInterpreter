package me.ialistannen.mimadebugger.gui.execution;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
   * @param uiUpdater updates the UI
   */
  abstract void run(MiMaRunner runner, Set<Integer> breakpoints,
      Supplier<Boolean> cancelledSupplier, Consumer<State> uiUpdater) throws MiMaException;

  /**
   * Performs a single step or stops the execution.
   *
   * @param runner the runner to use
   * @param breakpoints the breakpoints
   * @return the current step or en empty optional if it should terminate
   * @throws MiMaException if an error occurs or the MiMa should halt (ProgramHaltException)
   */
  protected Optional<State> singleStep(MiMaRunner runner, Set<Integer> breakpoints)
      throws MiMaException {
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
