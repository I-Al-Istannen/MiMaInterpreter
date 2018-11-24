package me.ialistannen.mimadebugger.gui.execution;

import java.io.IOException;
import java.util.Arrays;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.machine.ImmutableState;
import me.ialistannen.mimadebugger.machine.MiMa;
import me.ialistannen.mimadebugger.machine.MiMaRunner;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.machine.program.ProgramParser;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class ExecutionControls extends BorderPane {

  @FXML
  private Button prevStepButton;
  @FXML
  private Button nextStepButton;
  @FXML
  private Button executeButton;
  @FXML
  private Button loadProgramIntoMemory;

  private InstructionSet instructionSet;
  private Consumer<State> stateConsumer;
  private ProgramParser programParser;

  private ObjectProperty<MiMaRunner> runner;
  private BooleanProperty programOutOfDate;
  private BooleanProperty noPreviousStep;
  private SimpleStringProperty programTextProperty;

  private Set<Integer> breakpoints;

  public ExecutionControls(InstructionSet instructionSet) {
    this.instructionSet = instructionSet;
    this.stateConsumer = state -> {
    };
    this.programParser = new ProgramParser(instructionSet);
    this.runner = new SimpleObjectProperty<>();
    this.programOutOfDate = new SimpleBooleanProperty(false);
    this.programTextProperty = new SimpleStringProperty("");
    this.noPreviousStep = new SimpleBooleanProperty(true);
    this.breakpoints = new HashSet<>();

    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/gui/execution/ExecutionControls.fxml")
    );

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @FXML
  private void initialize() {
    BooleanBinding disableStepButtons = programOutOfDate.or(runner.isNull());

    nextStepButton.disableProperty().bind(disableStepButtons);
    prevStepButton.disableProperty().bind(disableStepButtons.or(noPreviousStep));
    executeButton.disableProperty().bind(disableStepButtons);

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
  public void setProgram(List<String> program) {
    List<InstructionCall> calls = programParser.parseFromNames(program);

    MainMemory memory = MainMemory.create();

    for (int i = 0; i < calls.size(); i++) {
      InstructionCall call = calls.get(i);

      memory = memory.set(i, MemoryFormat.combineInstruction(call));
    }

    State initialState = ImmutableState.builder()
        .memory(memory)
        .registers(ImmutableRegisters.builder().build())
        .build();

    MiMa miMa = new MiMa(initialState, instructionSet);

    runner.set(new MiMaRunner(miMa));

    programOutOfDate.set(false);
    stateConsumer.accept(initialState);
  }

  @FXML
  private void onNext() {
    stepGuardException(() -> stateConsumer.accept(runner.get().nextStep()));
    noPreviousStep.set(false);
  }

  @FXML
  private void onPrevious() {
    stepGuardException(() -> stateConsumer.accept(runner.get().previousStep()));

    if (!runner.get().hasPreviousStep()) {
      noPreviousStep.set(true);
    } else {
      noPreviousStep.set(false);
    }
  }

  @FXML
  private void onLoadProgram() {
    setProgram(Arrays.asList(programTextProperty.get().split("\\n")));
  }

  @FXML
  private void onExecute() {
    try {
      while (true) {
        // runner.nextStep will throw an exception when the program is finished
        State step = runner.get().nextStep();
        stateConsumer.accept(step);
        noPreviousStep.set(false);

        if (breakpoints.contains(step.registers().instructionPointer())) {
          displayMessageBreakpointHit(step.registers().instructionPointer());
          break;
        }
      }
    } catch (MiMaException e) {
      displayError(e);
    }
  }

  private void stepGuardException(Runnable runnable) {
    try {
      runnable.run();
    } catch (MiMaException e) {
      displayError(e);
    }
  }

  private void displayError(MiMaException e) {
    if (e instanceof ProgramHaltException) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Program exited");
      alert.setHeaderText("Program halted normally!");
      alert.show();
    } else {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error executing program");
      alert.setHeaderText(e.getMessage());
      alert.show();
    }
  }

  private void displayMessageBreakpointHit(int address) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Breakpoint hit");
    alert.setHeaderText("Breakpoint hit at " + address + "!");
    alert.show();
  }
}
