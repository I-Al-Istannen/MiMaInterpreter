package me.ialistannen.mimadebugger.machine;

import java.util.ArrayDeque;
import java.util.Deque;

public class MiMaRunner {

  private MiMa miMa;

  private Deque<State> previousStates;
  private Deque<State> nextStates;
  private State current;

  public MiMaRunner(MiMa miMa) {
    this.miMa = miMa;

    this.nextStates = new ArrayDeque<>();
    this.previousStates = new ArrayDeque<>();
  }

  public State nextStep() {
    if (nextStates.isEmpty()) {
      current = miMa.step();
      previousStates.push(current);
    } else {
      current = nextStates.pop();
    }

    return current;
  }

  public State previousStep() {
    if (previousStates.isEmpty() && current != null) {
      previousStates.push(current);
    } else if (!previousStates.isEmpty()) {
      current = previousStates.pop();
    }

    return current;
  }
}
