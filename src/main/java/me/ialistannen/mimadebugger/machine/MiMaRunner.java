package me.ialistannen.mimadebugger.machine;

import java.util.ArrayDeque;
import java.util.Deque;
import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;

public class MiMaRunner {

  private MiMa miMa;

  private Deque<State> previousStates;
  private Deque<State> nextStates;
  private State current;
  private State initial;

  public MiMaRunner(MiMa miMa) {
    this.miMa = miMa;
    this.current = miMa.getCurrentState();
    this.initial = miMa.getCurrentState();

    this.nextStates = new ArrayDeque<>(500);
    this.previousStates = new ArrayDeque<>(500);
  }

  /**
   * Returns the next step.
   *
   * This can either be the next one on the history or a newly computed step.
   *
   * @return the next step
   * @throws InstructionNotFoundException if the next instruction was invalid
   * @throws MiMaException if the instruction threw it
   */
  public State nextStep() throws MiMaException {
    if (!nextStates.isEmpty()) {
      previousStates.push(current);
      current = nextStates.pop();

      return current;
    }

    State nextStep = miMa.step();

    previousStates.push(current);
    if (previousStates.size() > 1000) {
      previousStates.pollLast();
    }

    current = nextStep;

    return current;
  }

  /**
   * Returns the previous step (or the current one, if there is no previous).
   *
   * @return the previous state
   */
  public State previousStep() {
    if (!previousStates.isEmpty()) {
      nextStates.push(current);

      current = previousStates.pop();

      return current;
    }

    // don't change things, there is no previous state
    return current;
  }

  /**
   * Returns whether there is a previous step.
   *
   * @return whether there is a previous step
   */
  public boolean hasPreviousStep() {
    return !previousStates.isEmpty();
  }

  /**
   * Returns whether there is a cached next step.
   *
   * @return whether there is a cached next step
   */
  public boolean hasCachedNextStep() {
    return !nextStates.isEmpty();
  }

  /**
   * Returns the current state.
   *
   * @return the current state
   */
  public State getCurrent() {
    return current;
  }

  /**
   * Resets the runner to the initial state, before any call to {@link #nextStep()} was made.
   *
   * @return the initial state that it was reset to
   */
  public State reset() {
    current = initial;
    previousStates.clear();
    nextStates.clear();

    miMa = miMa.copy(current);

    return current;
  }

  /**
   * Checks whether the program is finished.
   *
   * @return true if the program has finished executing
   */
  public boolean isFinished() {
    if (!nextStates.isEmpty()) {
      return false;
    }

    try {
      miMa.step();
      miMa = miMa.copy(current);
      return false;
    } catch (ProgramHaltException e) {
      return true;
    } catch (MiMaException e) {
      return false;
    }
  }

}
