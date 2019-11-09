package me.ialistannen.mimadebugger.gui.execution;

import com.jfoenix.controls.JFXComboBox;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.MiMaSyntaxError;
import me.ialistannen.mimadebugger.exceptions.NamedExecutionError;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.gui.util.FxmlUtil;
import me.ialistannen.mimadebugger.machine.ImmutableState;
import me.ialistannen.mimadebugger.machine.MiMa;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.parser.MiMaAssemblyParser;
import me.ialistannen.mimadebugger.parser.util.MiMaExceptionRunnable;

public class ExecutionControls extends BorderPane {

  private static final int MAXIMUM_STEP_COUNT = 1_000_000;

  @FXML
  private Button prevStepButton;
  @FXML
  private Button nextStepButton;
  @FXML
  private Button executeAndPauseButton;
  @FXML
  private Button loadProgramIntoMemory;
  @FXML
  private Button resetButton;
  @FXML
  private JFXComboBox<ExecutionStrategy> executionStrategySelection;

  private InstructionSet instructionSet;
  private Consumer<State> stateConsumer;
  private MiMaAssemblyParser programParser;

  private ObjectProperty<MiMaRunner> runner;
  private BooleanProperty programOutOfDate;
  private BooleanProperty noPreviousStep;
  private BooleanProperty noCachedNextStep;
  private BooleanProperty halted;
  private BooleanProperty currentlyRunning;
  private SimpleStringProperty programTextProperty;

  private Set<Integer> breakpoints;
  private Task<Void> executionTask;

  public ExecutionControls(InstructionSet instructionSet) {
    this.instructionSet = instructionSet;
    this.stateConsumer = state -> {
    };
    this.programParser = new MiMaAssemblyParser(instructionSet);
    this.runner = new SimpleObjectProperty<>();
    this.programOutOfDate = new SimpleBooleanProperty(false);
    this.programTextProperty = new SimpleStringProperty("");
    this.noPreviousStep = new SimpleBooleanProperty(true);
    this.noCachedNextStep = new SimpleBooleanProperty(false);
    this.halted = new SimpleBooleanProperty(false);
    this.currentlyRunning = new SimpleBooleanProperty(false);
    this.breakpoints = new HashSet<>();

    FxmlUtil.loadWithRoot(this, "/gui/execution/ExecutionControls.fxml");
  }


  @FXML
  private void initialize() {
    BooleanBinding disableStepButtons = programOutOfDate
        .or(runner.isNull());

    nextStepButton.disableProperty().bind(
        disableStepButtons
            .or(halted.and(noCachedNextStep))
            .or(currentlyRunning)
    );
    prevStepButton.disableProperty().bind(
        disableStepButtons.or(noPreviousStep)
            .or(currentlyRunning)
    );
    executeAndPauseButton.disableProperty().bind(
        disableStepButtons
            .or(halted.and(noCachedNextStep))
    );
    resetButton.disableProperty().bind(disableStepButtons);

    programTextProperty.addListener((observable, oldValue, newValue) -> programOutOfDate.set(true));

    disableStepButtons.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        stateConsumer.accept(null);
      }
    });

    currentlyRunning.addListener(
        (ob, ov, running) -> executeAndPauseButton.setText(running ? "Stop" : "Execute")
    );

    executionStrategySelection.getItems().setAll(
        new LimitedStepsExecutionStrategy(MAXIMUM_STEP_COUNT),
        new RunForeverExecutionStrategy(),
        new ClockExecutionStrategy(500)
    );
    executionStrategySelection.getSelectionModel().select(0);
  }

  /**
   * Sets the consumer that is called for each new state.
   *
   * @param stateConsumer the consumer that is called for each new state. Null indicates that no
   *     program is being executed
   */
  public void setStateConsumer(Consumer<State> stateConsumer) {
    this.stateConsumer = stateConsumer;
  }

  /**
   * Returns the property holding the text of the program.
   *
   * @return the program text property
   */
  public SimpleStringProperty programTextPropertyProperty() {
    return programTextProperty;
  }

  /**
   * Returns the current state of the MiMa. If no program is loaded, it will just convert the
   * program in the editor to a state.
   *
   * @return the current state
   */
  public State getCurrentState() throws MiMaSyntaxError {
    if (runner.get() == null || programOutOfDate.get()) {
      List<MemoryValue> memoryValues = new MiMaAssemblyParser(instructionSet)
          .parseProgramToMemoryValues(programTextProperty.get());
      return ImmutableState.builder()
          .memory(MainMemory.create(memoryValues))
          .registers(ImmutableRegisters.builder().build())
          .build();
    }
    return runner.get().getCurrent();
  }

  /**
   * Adds a breakpoint where program execution halts, when the execute button was pressed.
   *
   * @param breakpoint the breakpoint where program execution halts
   */
  public void addBreakpoint(int breakpoint) {
    this.breakpoints.add(breakpoint);
  }

  /**
   * Removes a breakpoint where program execution halts.
   *
   * @param breakpoint the breakpoint where program execution halts
   */
  public void removeBreakpoint(int breakpoint) {
    this.breakpoints.remove(breakpoint);
  }

  /**
   * Sets the program to execute
   *
   * @param program the program to execute
   * @throws MiMaSyntaxError if the program has an syntax error
   * @throws NumberOverflowException if the program's addresses were too large
   */
  private void setProgram(String program) throws MiMaSyntaxError, NumberOverflowException {
    List<MemoryValue> values = programParser.parseProgramToMemoryValues(program);

    MainMemory memory = MainMemory.create();

    for (MemoryValue value : values) {
      memory = memory.set(value.address(), value.representation());
    }

    State initialState = ImmutableState.builder()
        .memory(memory)
        .registers(ImmutableRegisters.builder().build())
        .build();

    MiMa miMa = new MiMa(initialState, instructionSet);

    runner.set(new MiMaRunner(miMa));

    programOutOfDate.set(false);

    reset();
  }

  @FXML
  private void onNext() {
    stepGuardException(() -> stateConsumer.accept(runner.get().nextStep()));

    afterStep();
  }

  @FXML
  private void onPrevious() {
    stepGuardException(() -> stateConsumer.accept(runner.get().previousStep()));

    afterStep();
  }

  @FXML
  private void onLoadProgram() {
    try {
      setProgram(programTextProperty.get());
    } catch (MiMaException e) {
      onError(e);
    }
  }

  @FXML
  private void onReset() {
    reset();
  }

  private void reset() {
    stateConsumer.accept(runner.get().reset());
    noPreviousStep.set(!runner.get().hasPreviousStep());
    halted.set(false);
  }

  @FXML
  private void onExecute() {
    if (executionTask != null && executionTask.isRunning()) {
      executionTask.cancel();
      currentlyRunning.set(false);
      return;
    }
    if (runner.get().isFinished()) {
      reset();
    }
    ExecutionStrategy strategy = executionStrategySelection.getSelectionModel().getSelectedItem();

    executionTask = new Task<Void>() {
      Instant now = Instant.now();

      @Override
      protected Void call() throws MiMaException {
        strategy.run(
            runner.get(),
            breakpoints,
            this::isCancelled,
            new DebouncingUiRunnable<>(stateConsumer, Duration.ofMillis(250))
        );
        return null;
      }

      @Override
      protected void failed() {
        Throwable exception = getException();
        onError(exception);

        afterExec();
      }

      @Override
      protected void succeeded() {
        afterExec();
      }

      @Override
      protected void cancelled() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Cancelled");
        alert.setHeaderText("I tried to fulfill your wish");
        displayAlert(alert);
      }

      private void afterExec() {
        System.out.println(Duration.between(now, Instant.now()));

        // only set it once at the end
        stateConsumer.accept(runner.get().getCurrent());
        afterStep();
        currentlyRunning.set(false);
      }
    };

    currentlyRunning.set(true);
    Thread worker = new Thread(executionTask);
    worker.setDaemon(true);
    worker.start();
  }

  private void stepGuardException(MiMaExceptionRunnable runnable) {
    try {
      runnable.run();
    } catch (MiMaException e) {
      onError(e);
    }
  }

  private void onError(Throwable e) {
    displayExecutionError(e);
    if (e instanceof ProgramHaltException) {
      halted.set(true);
    }
  }

  private void displayExecutionError(Throwable e) {
    Alert alert;
    if (e instanceof ProgramHaltException) {
      alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Program exited");
      alert.setHeaderText("Program halted normally!");
    } else if (e instanceof NamedExecutionError) {
      NamedExecutionError excError = (NamedExecutionError) e;
      alert = new Alert(AlertType.WARNING);
      alert.setTitle(excError.getName());
      alert.setHeaderText(excError.getName());
      alert.setContentText(e.getMessage());
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error executing program");
      alert.setHeaderText(e.getMessage());
    }

    displayAlert(alert);
  }

  private void displayAlert(Alert alert) {
    alert.setResizable(true);
    alert.initOwner(getScene().getWindow());
    alert.show();
  }

  private void afterStep() {
    noPreviousStep.set(!runner.get().hasPreviousStep());
    noCachedNextStep.set(!runner.get().hasCachedNextStep());
  }
}
