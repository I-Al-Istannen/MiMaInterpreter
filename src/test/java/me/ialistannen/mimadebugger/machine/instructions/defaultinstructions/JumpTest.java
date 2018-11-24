package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Jump.JUMP;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Jump.JUMP_IF_NEGATIVE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class JumpTest extends InstructionTest {

  private static final int JUMP_TARGET = 30;
  private static final int NO_JUMP = 0;

  @Test
  void testUnconditionalJumpWithMinusOne() {
    assertThat(
        getJumpAddress(-1, JUMP),
        is(JUMP_TARGET)
    );
  }

  @Test
  void testUnconditionalJumpWithPositiveValue() {
    assertThat(
        getJumpAddress(1, JUMP),
        is(JUMP_TARGET)
    );
  }

  @Test
  void testUnconditionalJumpWithZero() {
    assertThat(
        getJumpAddress(0, JUMP),
        is(JUMP_TARGET)
    );
  }

  @Test
  void testUnconditionalJumpWithNegativeNumber() {
    assertThat(
        getJumpAddress(-32, JUMP),
        is(JUMP_TARGET)
    );
  }


  @Test
  void testConditionalJumpWithMinusOne() {
    assertThat(
        getJumpAddress(-1, JUMP_IF_NEGATIVE),
        is(JUMP_TARGET)
    );
  }

  @Test
  void testConditionalJumpWithPositiveValue() {
    assertThat(
        getJumpAddress(1, JUMP_IF_NEGATIVE),
        is(NO_JUMP)
    );
  }

  @Test
  void testConditionalJumpWithMaximum() {
    assertThat(
        getJumpAddress(MemoryFormat.VALUE_MAXIMUM, JUMP_IF_NEGATIVE),
        is(NO_JUMP)
    );
  }

  @Test
  void testConditionalJumpWithZero() {
    assertThat(
        getJumpAddress(0, JUMP_IF_NEGATIVE),
        is(NO_JUMP)
    );
  }

  @Test
  void testConditionalJumpWithNegativeNumber() {
    assertThat(
        getJumpAddress(-32, JUMP_IF_NEGATIVE),
        is(JUMP_TARGET)
    );
  }

  @Test
  void testConditionalJumpWithMinimum() {
    assertThat(
        getJumpAddress(MemoryFormat.VALUE_MINIMUM, JUMP_IF_NEGATIVE),
        is(JUMP_TARGET)
    );
  }

  private int getJumpAddress(int accumulator, Instruction jumpInstruction) {
    State state = getState().copy()
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        );

    return jumpInstruction.apply(state, JUMP_TARGET).registers().instructionPointer();
  }

}