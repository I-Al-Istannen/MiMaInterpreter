package me.ialistannen.mimadebugger.machine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Arithmetic;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Load;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Special;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Store;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiMaRunnerTest {

  private MiMaRunner runner;
  private MiMa miMa;

  @BeforeEach
  void setup() throws NumberOverflowException {
    InstructionSet instructionSet = new InstructionSet();

    MainMemory memory = MainMemory.create()
        .set(
            0, MemoryFormat.combineInstruction(toCall(Load.LOAD_CONSTANT, 20))
        )
        .set(
            1, MemoryFormat.combineInstruction(toCall(Store.STORE, 20))
        )
        .set(
            2, MemoryFormat.combineInstruction(toCall(Arithmetic.ADD, 20))
        )
        .set(
            3, MemoryFormat.combineInstruction(toCall(Special.HALT, 0))
        );
    State initialState = ImmutableState.builder()
        .registers(ImmutableRegisters.builder().build())
        .memory(memory)
        .build();

    miMa = new MiMa(initialState, instructionSet);

    this.runner = new MiMaRunner(miMa);
  }

  @Test
  void testStepOnce() throws MiMaException {
    assertThat(runner.nextStep()).isEqualTo(miMa.getCurrentState());
  }

  @Test
  void testThrowsHaltExceptionWhenFinished() throws MiMaException {
    runner.nextStep();
    runner.nextStep();
    runner.nextStep();

    assertThrows(
        ProgramHaltException.class,
        () -> runner.nextStep()
    );
  }

  @Test
  void reportsWhenFinished() throws MiMaException {
    runner.nextStep();
    runner.nextStep();
    runner.nextStep();

    State currentState = miMa.getCurrentState();

    assertThat(runner.isFinished()).isTrue();

    // Not broken when stepping back again (i.e. did not mutate state) when checking
    runner.previousStep();
    assertThat(runner.nextStep()).isEqualTo(currentState);
  }

  @Test
  void reportsWhenNotYetFinishedCached() throws MiMaException {
    runner.nextStep();
    runner.nextStep();
    State lastState = runner.nextStep();
    // go back
    runner.previousStep();

    assertThat(runner.isFinished()).isFalse();

    // Not broken when stepping back again (i.e. did not mutate state) when checking
    assertThat(runner.nextStep()).isEqualTo(lastState);
  }

  @Test
  void reportsWhenNotYetFinishedComputed() throws MiMaException {
    runner.nextStep();
    runner.nextStep();

    assertThat(runner.isFinished()).isFalse();
  }

  @Test
  void testReset() throws MiMaException {
    State initialState = miMa.getCurrentState();
    List<State> steps = Arrays.asList(
        runner.nextStep(),
        runner.nextStep(),
        runner.nextStep()
    );

    assertThat(runner.reset()).isEqualTo(initialState);
    assertThat(Arrays.asList(runner.nextStep(), runner.nextStep(), runner.nextStep())).isEqualTo(
        steps);
  }


  @Test
  void testHasNoPrevious() {
    assertThat(runner.hasPreviousStep()).isFalse();
  }

  @Test
  void testHasNoCachedNext() {
    assertThat(runner.hasCachedNextStep()).isFalse();
  }

  @Test
  void testPreviousOfFirstIsFirst() {
    assertThat(runner.hasPreviousStep()).isFalse();
    State initialState = miMa.getCurrentState();
    assertThat(runner.previousStep()).isEqualTo(initialState);
  }

  @Test
  void testStepToEnd() {
    assertThrows(
        ProgramHaltException.class,
        () -> {
          for (int i = 0; i < 10; i++) {
            assertThat(runner.nextStep()).isEqualTo(miMa.getCurrentState());
          }
        }
    );
  }

  @Test
  void testStepToEndHasPreviousSteps() {
    assertThrows(
        ProgramHaltException.class,
        () -> {
          for (int i = 0; i < 10; i++) {
            assertThat(runner.nextStep()).isEqualTo(miMa.getCurrentState());

            if (i > 0) {
              assertThat(runner.hasPreviousStep()).isTrue();
            }
          }
        }
    );
  }

  @Test
  void testCanStepBackToBeginning() throws MiMaException {
    Deque<State> cachedStates = new ArrayDeque<>();
    cachedStates.push(miMa.getCurrentState());
    // go to end
    for (int i = 0; i < 3; i++) {
      cachedStates.push(runner.nextStep());
    }

    // last state is not duplicated
    cachedStates.pop();

    for (int i = 0; !cachedStates.isEmpty(); i++) {
      State previousStep = runner.previousStep();

      assertThat(runner.hasPreviousStep()).isEqualTo(i != 2);

      assertThat(runner.hasCachedNextStep()).isTrue();
      assertThat(previousStep).isEqualTo(cachedStates.pop());
    }
  }

  @Test
  void testCanStepBackAndSwitchDirection() throws MiMaException {
    List<State> cachedStates = new ArrayList<>();
    cachedStates.add(miMa.getCurrentState());
    // go to end
    for (int i = 0; i < 3; i++) {
      cachedStates.add(runner.nextStep());
    }

    // one backwards
    assertThat(runner.previousStep()).isEqualTo(cachedStates.get(2));
    assertThat(runner.hasCachedNextStep()).isTrue();

    State currentMiMaState = miMa.getCurrentState();

    // one forwards
    assertThat(runner.nextStep()).isEqualTo(cachedStates.get(3));

    // did not recompute MiMa state
    assertThat(miMa.getCurrentState()).isEqualTo(currentMiMaState);
  }

  private InstructionCall toCall(Instruction instruction, int argument) {
    return ImmutableInstructionCall.builder()
        .command(instruction)
        .argument(argument)
        .build();
  }

}
