package me.ialistannen.mimadebugger.machine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
  void setup() {
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
  void testStepOnce() {
    assertThat(
        runner.nextStep(),
        is(miMa.getCurrentState())
    );
  }

  @Test
  void testThrowsHaltExceptionWhenFinished() {
    runner.nextStep();
    runner.nextStep();
    runner.nextStep();

    assertThrows(
        ProgramHaltException.class,
        () -> runner.nextStep()
    );
  }


  @Test
  void testHasNoPrevious() {
    assertThat(
        runner.hasPreviousStep(),
        is(false)
    );
  }

  @Test
  void testStepToEnd() {
    assertThrows(
        ProgramHaltException.class,
        () -> {
          for (int i = 0; i < 10; i++) {
            assertThat(
                runner.nextStep(),
                is(miMa.getCurrentState())
            );
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
            assertThat(
                runner.nextStep(),
                is(miMa.getCurrentState())
            );

            if (i > 0) {
              assertThat(
                  runner.hasPreviousStep(),
                  is(true)
              );
            }
          }
        }
    );
  }

  @Test
  void testCanStepBackToBeginning() {
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

      assertThat(
          runner.hasPreviousStep(),
          is(i != 2)
      );
      assertThat(
          previousStep,
          is(cachedStates.pop())
      );
    }
  }

  @Test
  void testCanStepBackAndSwitchDirection() {
    List<State> cachedStates = new ArrayList<>();
    cachedStates.add(miMa.getCurrentState());
    // go to end
    for (int i = 0; i < 3; i++) {
      cachedStates.add(runner.nextStep());
    }

    // one backwards
    assertThat(
        runner.previousStep(),
        is(cachedStates.get(2))
    );

    State currentMiMaState = miMa.getCurrentState();

    // one forwards
    assertThat(
        runner.nextStep(),
        is(cachedStates.get(3))
    );

    // did not recompute mima state
    assertThat(
        miMa.getCurrentState(),
        is(currentMiMaState)
    );
  }

  private InstructionCall toCall(Instruction instruction, int argument) {
    return ImmutableInstructionCall.builder()
        .command(instruction)
        .argument(argument)
        .build();
  }

}