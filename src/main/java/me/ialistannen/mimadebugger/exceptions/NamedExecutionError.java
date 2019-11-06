package me.ialistannen.mimadebugger.exceptions;

public class NamedExecutionError extends MiMaException {

  private String name;

  public NamedExecutionError(String message, String name) {
    super(message);
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
