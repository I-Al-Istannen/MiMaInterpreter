package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.ImmutableState;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StackTest extends InstructionTest {


  @ParameterizedTest(name = "Storing {0} from SP to acc")
  @CsvSource({
      "20",     // random values
      "0",      // ip   address min
      "65535",  // ip   address max
  })
  void spToAcc(int returnAddress) {
    State state = getState().copy().withRegisters(
        getState().registers().copy().withStackPointer(returnAddress)
    );
    State resultState = Stack.LDSP.apply(state, 0);

    assertThat(
        resultState,
        is(
            state.copy().withRegisters(
                state.registers().copy()
                    .withStackPointer(returnAddress)
                    .withAccumulator(returnAddress)
            )
        )
    );
  }

  @ParameterizedTest(name = "Loading {0} from acc to SP")
  @CsvSource({
      "20",     // Random values
      "0",      // ip   address min
      "65535",  // ip   address max
  })
  void accToSP(int accumulator) {
    State state = getStateWithAccumulator(accumulator);
    State resultState = Stack.STSP.apply(state, 0);

    assertThat(
        resultState,
        is(
            state.copy().withRegisters(
                state.registers().copy()
                    .withStackPointer(accumulator)
                    .withAccumulator(accumulator)
            )
        )
    );
  }

  @Test
  void accToSPHandlesOverflow() {
    assertThrows(
        NumberOverflowException.class,
        () -> Stack.STSP.apply(getStateWithAccumulator(1 << MemoryFormat.ADDRESS_LENGTH + 1), 0)
    );

    assertThrows(
        NumberOverflowException.class,
        () -> Stack.STSP.apply(getStateWithAccumulator(-1), 0)
    );
  }


  @ParameterizedTest(name = "Storing {0} from FP to acc")
  @CsvSource({
      "20",     // random values
      "0",      // ip   address min
      "65535",  // ip   address max
  })
  void fpToAcc(int returnAddress) {
    State state = getState().copy().withRegisters(
        getState().registers().copy().withFramePointer(returnAddress)
    );
    State resultState = Stack.LDFP.apply(state, 0);

    assertThat(
        resultState,
        is(
            state.copy().withRegisters(
                state.registers().copy()
                    .withFramePointer(returnAddress)
                    .withAccumulator(returnAddress)
            )
        )
    );
  }

  @ParameterizedTest(name = "Loading {0} from acc to FP")
  @CsvSource({
      "20",     // Random values
      "0",      // ip   address min
      "65535",  // ip   address max
  })
  void accToFP(int accumulator) {
    State state = getStateWithAccumulator(accumulator);
    State resultState = Stack.STFP.apply(state, 0);

    assertThat(
        resultState,
        is(
            state.copy().withRegisters(
                state.registers().copy()
                    .withFramePointer(accumulator)
                    .withAccumulator(accumulator)
            )
        )
    );
  }

  @Test
  void accToFPHandlesOverflow() {
    assertThrows(
        NumberOverflowException.class,
        () -> Stack.STFP.apply(getStateWithAccumulator(1 << MemoryFormat.ADDRESS_LENGTH + 1), 0)
    );

    assertThrows(
        NumberOverflowException.class,
        () -> Stack.STFP.apply(getStateWithAccumulator(-1), 0)
    );
  }

  @ParameterizedTest(name = "Loading {1} with an offset of {0}+{2}")
  @CsvSource({
      "65535, 20, 0",     // load from stack pointer
      "65535, -20, 0",    // load negative from stack pointer
      "65535, -1, 0",     // load negative from stack pointer
      "65535, 20, -10",   // negative offset
      "65535, 20, 10",    // positive offset
      "0, 20, 1048575",   // barely legal large offset (1)
      "0, 20, 1048555",   // barely legal large offset (20)
      "10, 20, -5",       // barely legal large offset (-5)
  })
  void loadRelativeToSP(int stackPointerAddress, int valueToLoad, int offset) {
    State state = getState().copy()
        .withMemory(MainMemory.create().set(stackPointerAddress + offset, valueToLoad))
        .withRegisters(
            getState().registers().copy().withStackPointer(stackPointerAddress)
        );

    assertThat(
        Stack.LDVR.apply(state, offset),
        is(
            state.copy().withRegisters(
                state.registers().copy().withAccumulator(valueToLoad)
            )
        )
    );
  }

  @Test
  void loadRelativeToSPHandlesOverflow() {
    assertThrows(
        NumberOverflowException.class,
        () -> Stack.LDVR.apply(getStateWithStackPointer(0), 0xFFFFF + 1)
    );

    assertThrows(
        NumberOverflowException.class,
        () -> Stack.LDVR.apply(getStateWithStackPointer(20), 0xFFFFF - 19)
    );
  }

  @ParameterizedTest(name = "Storing {1} with an offset of {0}+{2}")
  @CsvSource({
      "65535, 20, 0",     // store to stack pointer
      "65535, -20, 0",    // store negative from stack pointer
      "65535, -1, 0",     // store negative from stack pointer
      "65535, 20, -10",   // negative offset
      "65535, 20, 10",    // positive offset
      "0, 20, 1048575",   // barely legal large offset (1)
      "0, 20, 1048555",   // barely legal large offset (20)
      "10, 20, -5",       // barely legal large offset (-5)
  })
  void storeRelativeToSP(int stackPointerAddress, int valueToStore, int offset) {
    State state = getState().copy()
        .withRegisters(
            getState().registers().copy()
                .withStackPointer(stackPointerAddress)
                .withAccumulator(valueToStore)
        );

    assertThat(
        Stack.STVR.apply(state, offset),
        is(
            state.copy().withMemory(
                MainMemory.create().set(stackPointerAddress + offset, valueToStore)
            )
        )
    );
  }

  @Test
  void storeRelativeToSPHandlesOverflow() {
    assertThrows(
        NumberOverflowException.class,
        () -> Stack.STVR.apply(getStateWithStackPointer(0), 0xFFFFF + 1)
    );

    assertThrows(
        NumberOverflowException.class,
        () -> Stack.STVR.apply(getStateWithStackPointer(20), 0xFFFFF - 19)
    );
  }

  private ImmutableState getStateWithAccumulator(int accumulator) {
    return getState().copy().withRegisters(
        getState().registers().copy().withAccumulator(accumulator)
    );
  }

  private ImmutableState getStateWithStackPointer(int stackPointer) {
    return getState().copy().withRegisters(
        getState().registers().copy().withStackPointer(stackPointer)
    );
  }


}