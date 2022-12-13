package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Jump.JUMP;
import static me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Jump.JUMP_IF_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;

import me.ialistannen.mimadebugger.exceptions.MiMaException;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class JumpTest extends InstructionTest {

  private static final int JUMP_TARGET = 30;
  private static final int NO_JUMP = 0;

  @Test
  void testUnconditionalJumpWithMinusOne() throws MiMaException {
    assertThat(getJumpAddress(-1, JUMP)).isEqualTo(JUMP_TARGET);
  }

  @Test
  void testUnconditionalJumpWithPositiveValue() throws MiMaException {
    assertThat(getJumpAddress(1, JUMP)).isEqualTo(JUMP_TARGET);
  }

  @Test
  void testUnconditionalJumpWithZero() throws MiMaException {
    assertThat(getJumpAddress(0, JUMP)).isEqualTo(JUMP_TARGET);
  }

  @Test
  void testUnconditionalJumpWithNegativeNumber() throws MiMaException {
    assertThat(getJumpAddress(-32, JUMP)).isEqualTo(JUMP_TARGET);
  }


  @Test
  void testConditionalJumpWithMinusOne() throws MiMaException {
    assertThat(getJumpAddress(-1, JUMP_IF_NEGATIVE)).isEqualTo(JUMP_TARGET);
  }

  @Test
  void testConditionalJumpWithPositiveValue() throws MiMaException {
    assertThat(getJumpAddress(1, JUMP_IF_NEGATIVE)).isEqualTo(NO_JUMP);
  }

  @Test
  void testConditionalJumpWithMaximum() throws MiMaException {
    assertThat(getJumpAddress(MemoryFormat.VALUE_MAXIMUM, JUMP_IF_NEGATIVE)).isEqualTo(NO_JUMP);
  }

  @Test
  void testConditionalJumpWithZero() throws MiMaException {
    assertThat(getJumpAddress(0, JUMP_IF_NEGATIVE)).isEqualTo(NO_JUMP);
  }

  @Test
  void testConditionalJumpWithNegativeNumber() throws MiMaException {
    assertThat(getJumpAddress(-32, JUMP_IF_NEGATIVE)).isEqualTo(JUMP_TARGET);
  }

  @Test
  void testConditionalJumpWithMinimum() throws MiMaException {
    assertThat(getJumpAddress(MemoryFormat.VALUE_MINIMUM, JUMP_IF_NEGATIVE)).isEqualTo(JUMP_TARGET);
  }

  private int getJumpAddress(int accumulator, Instruction jumpInstruction) throws MiMaException {
    State state = getState().copy()
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        );

    return jumpInstruction.apply(state, JUMP_TARGET).registers().instructionPointer();
  }

}
