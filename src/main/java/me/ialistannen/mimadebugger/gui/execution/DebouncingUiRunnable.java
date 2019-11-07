package me.ialistannen.mimadebugger.gui.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import javafx.application.Platform;

public class DebouncingUiRunnable<T> implements Consumer<T> {

  private Consumer<T> action;
  private Instant lastUpdate;
  private Duration debounceDuration;

  public DebouncingUiRunnable(Consumer<T> action, Duration debounceDuration) {
    this.action = action;
    this.lastUpdate = Instant.now();
    this.debounceDuration = debounceDuration;
  }

  @Override
  public void accept(T t) {
    if (Duration.between(lastUpdate, Instant.now()).compareTo(debounceDuration) >= 0) {
      Platform.runLater(() -> action.accept(t));
      lastUpdate = Instant.now();
    }
  }
}
