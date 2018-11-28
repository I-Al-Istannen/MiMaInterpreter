package me.ialistannen.mimadebugger.gui.execution;

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
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
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

public class ExecutionControls extends BorderPane {

  private static final int MAXIMUM_STEP_COUNT = 1_000_000;

  @FXML
  private Button prevStepButton;
  @FXML
  private Button nextStepButton;
  @FXML
  private Button executeButton;
  @FXML
  private Button loadProgramIntoMemory;
  @FXML
  private Button resetButton;

  private InstructionSet instructionSet;
  private Consumer<State> stateConsumer;
  private MiMaAssemblyParser programParser;

  private ObjectProperty<MiMaRunner> runner;
  private BooleanProperty programOutOfDate;
  private BooleanProperty noPreviousStep;
  private BooleanProperty noCachedNextStep;
  private BooleanProperty halted;
  private SimpleStringProperty programTextProperty;

  private Set<Integer> breakpoints;

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
    this.breakpoints = new HashSet<>();

    FxmlUtil.loadWithRoot(this, "/gui/execution/ExecutionControls.fxml");
  }


  @FXML
  private void initialize() {
    BooleanBinding disableStepButtons = programOutOfDate
        .or(runner.isNull());

    nextStepButton.disableProperty().bind(disableStepButtons.or(halted.and(noCachedNextStep)));
    prevStepButton.disableProperty().bind(disableStepButtons.or(noPreviousStep));
    executeButton.disableProperty().bind(disableStepButtons.or(halted.and(noCachedNextStep)));
    resetButton.disableProperty().bind(disableStepButtons);

    programTextProperty.addListener((observable, oldValue, newValue) -> programOutOfDate.set(true));

    disableStepButtons.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        stateConsumer.accept(null);
      }
    });
  }

  /**
   * Sets the consumer that is called for each new state.
   *
   * @param stateConsumer the consumer that is called for each new state. Null indicates that no
   * program is being executed
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
   */
  private void setProgram(String program) {
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
    try {
      if (runner.get().isFinished()) {
        reset();
      }

      for (int i = 0; i < MAXIMUM_STEP_COUNT; i++) {
        // runner.nextStep will throw an exception when the program is finished
        State step = runner.get().nextStep();

        if (breakpoints.contains(step.registers().instructionPointer())) {
          return;
        }
      }
      displayStepsExceededMessage();
    } catch (MiMaException e) {
      onError(e);
    } finally {
      // only set it once at the end
      stateConsumer.accept(runner.get().getCurrent());
      afterStep();
    }
  }

  private void displayStepsExceededMessage() {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Execution stopped");
    alert.setHeaderText(String.format(
        "The execution exceeded %d steps.", MAXIMUM_STEP_COUNT
    ));
    alert.setContentText(
        "You can resume execution by clicking Execute again\n"
            + "(or by stepping), but it's probably an infinite loop."
    );
    displayAlert(alert);
  }

  private void stepGuardException(Runnable runnable) {
    try {
      runnable.run();
    } catch (MiMaException e) {
      onError(e);
    }
  }

  private void onError(MiMaException e) {
    displayExecutionError(e);
    if (e instanceof ProgramHaltException) {
      halted.set(true);
    }
  }

  private void displayExecutionError(MiMaException e) {
    Alert alert;
    if (e instanceof ProgramHaltException) {
      alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Program exited");
      alert.setHeaderText("Program halted normally!");
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
