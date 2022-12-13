package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.ImmutableState;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FunctionsTest extends InstructionTest {

  @ParameterizedTest(name = "Jumping from {0} to {1}")
  @CsvSource({
      "20, 50",     // random values
      "0, 50",      // ip   address min
      "20, 0",      // call address min
      "65535, 20",  // ip   address max
      "20, 65535",  // ip   address max
  })
  void callSetsReturnAddressAndIP(int ipAddress, int callAddress) throws MiMaException {
    State state = getState().copy().withRegisters(
        getState().registers().copy().withInstructionPointer(ipAddress)
    );
    State resultState = Functions.CALL.apply(state, callAddress);

    assertThat(resultState).isEqualTo(
        state.copy().withRegisters(
            state.registers().copy()
                .withInstructionPointer(callAddress)
                .withReturnAddress(ipAddress)
        )
    );
  }

  @Test
  void callErrorsOnTooLargeAddress() {
    State state = getState().copy().withRegisters(
        getState().registers().copy().withInstructionPointer(20)
    );
    assertThrows(
        NumberOverflowException.class,
        () -> Functions.CALL.apply(state, 1 << 24 + 1)
    );
  }

  @ParameterizedTest(name = "Returning from {0} to {1}")
  @CsvSource({
      "20, 50",     // random values
      "0, 50",      // ip   address min
      "20, 0",      // call address min
      "65535, 20",  // ip   address max
      "20, 65535",  // ip   address max
  })
  void retBasic(int startIpAddress, int retAddress) throws MiMaException {
    State state = getState().copy().withRegisters(
        getState().registers().copy()
            .withInstructionPointer(startIpAddress)
            .withReturnAddress(retAddress)
    );
    State resultState = Functions.RET.apply(state, 0);

    assertThat(resultState).isEqualTo(
        state.copy().withRegisters(
            state.registers().copy().withInstructionPointer(retAddress)
        )
    );
  }

  @ParameterizedTest(name = "Storing {0} from RA to acc")
  @CsvSource({
      "20",     // random values
      "0",      // ip   address min
      "65535",  // ip   address max
  })
  void raToAcc(int returnAddress) throws MiMaException {
    State state = getState().copy().withRegisters(
        getState().registers().copy().withReturnAddress(returnAddress)
    );
    State resultState = Functions.LDRA.apply(state, 0);

    assertThat(resultState).isEqualTo(
        state.copy().withRegisters(
            state.registers().copy()
                .withReturnAddress(returnAddress)
                .withAccumulator(returnAddress)
        )
    );
  }

  @ParameterizedTest(name = "Loading {0} from acc")
  @CsvSource({
      "20",     // random values
      "0",      // ip   address min
      "65535",  // ip   address max
  })
  void accToRa(int accumulator) throws MiMaException {
    State state = getStateWithAccumulator(accumulator);
    State resultState = Functions.STRA.apply(state, 0);

    assertThat(resultState).isEqualTo(
        state.copy().withRegisters(
            state.registers().copy()
                .withReturnAddress(accumulator)
                .withAccumulator(accumulator)
        )
    );
  }

  @Test
  void accToRaHandlesOverflow() {
    assertThrows(
        NumberOverflowException.class,
        () -> Functions.STRA.apply(getStateWithAccumulator(1 << MemoryFormat.ADDRESS_LENGTH + 1), 0)
    );

    assertThrows(
        NumberOverflowException.class,
        () -> Functions.STRA.apply(getStateWithAccumulator(-1), 0)
    );
  }

  private ImmutableState getStateWithAccumulator(int accumulator) {
    return getState().copy().withRegisters(
        getState().registers().copy().withAccumulator(accumulator)
    );
  }

}
