package me.ialistannen.mimadebugger.machine;

import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.util.RingBuffer;

public class MiMaRunner {

  private MiMa miMa;

  private State current;
  private State initial;
  private RingBuffer<State> bufferedStates;

  public MiMaRunner(MiMa miMa) {
    this.miMa = miMa;
    this.current = miMa.getCurrentState();
    this.initial = miMa.getCurrentState();

    this.bufferedStates = new RingBuffer<>(10_000);
    this.bufferedStates.offer(current);
  }

  /**
   * Returns the next step.
   *
   * This can either be the next one on the history or a newly computed step.
   *
   * @return the next step
   * @throws ProgramHaltException if there is no next step
   */
  public State nextStep() {
    if (bufferedStates.hasValueAfterView()) {
      bufferedStates.viewForwards();
      return bufferedStates.getValueAtView();
    }

    current = miMa.step();

    bufferedStates.offer(current);

    return current;
  }

  /**
   * Returns the previous step (or the current one, if there is no previous).
   *
   * @return the previous state
   */
  public State previousStep() {
    if (bufferedStates.hasValueBeforeView()) {
      bufferedStates.viewBackwards();
      return bufferedStates.getValueAtView();
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
    return bufferedStates.hasValueBeforeView();
  }

  /**
   * Returns whether there is a cached next step.
   *
   * @return whether there is a cached next step
   */
  public boolean hasCachedNextStep() {
    return bufferedStates.hasValueAfterView();
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
    bufferedStates.clear();
    bufferedStates.offer(current);

    miMa = miMa.copy(current);

    return current;
  }

  /**
   * Checks whether the program is finished.
   *
   * @return true if the program has finished executing
   */
  public boolean isFinished() {
    if (bufferedStates.hasValueAfterView()) {
      return false;
    }
    try {
      miMa.step();
      miMa = miMa.copy(current);
      return false;
    } catch (ProgramHaltException e) {
      return true;
    }
  }

}
