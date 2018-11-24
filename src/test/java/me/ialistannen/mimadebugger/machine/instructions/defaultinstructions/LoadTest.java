package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class LoadTest extends InstructionTest {

  //<editor-fold desc="LDC">
  @Test
  void testLoadNegativeNumber() {
    assertThrows(NumberOverflowException.class, () -> loadConstantResult(-1));
  }

  @Test
  void testLoadZero() {
    assertThat(
        loadConstantResult(0),
        is(0)
    );
  }

  @Test
  void testLoadOne() {
    assertThat(
        loadConstantResult(1),
        is(1)
    );
  }

  @Test
  void testLoadMaximumAddress() {
    assertThat(
        loadConstantResult(1 << MemoryFormat.ADDRESS_LENGTH - 1),
        is(1 << MemoryFormat.ADDRESS_LENGTH - 1)
    );
  }


  @Test
  void testLoadBiggerNumber() {
    assertThrows(
        NumberOverflowException.class,
        () -> loadConstantResult(1 << MemoryFormat.ADDRESS_LENGTH)
    );
  }

  @Test
  void testLoadMaximum() {
    assertThrows(
        NumberOverflowException.class,
        () -> loadConstantResult(MemoryFormat.VALUE_MAXIMUM)
    );
  }
  //</editor-fold>

  //<editor-fold desc="LDV">
  @Test
  void testLoadAddressNegativeNumber() {
    assertThat(
        loadFromMemoryResult(-1),
        is(-1)
    );
  }

  @Test
  void testLoadAddressMinimum() {
    assertThat(
        loadFromMemoryResult(MemoryFormat.VALUE_MINIMUM),
        is(MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testLoadAddressUnderflowNoException() {
    assertThat(
        loadFromMemoryResult(MemoryFormat.VALUE_MINIMUM - 1),
        is(Math.floorMod(MemoryFormat.VALUE_MINIMUM - 1, 1 << MemoryFormat.VALUE_LENGTH))
    );
  }

  @Test
  void testLoadAddressZero() {
    assertThat(
        loadFromMemoryResult(0),
        is(0)
    );
  }

  @Test
  void testLoadAddressOne() {
    assertThat(
        loadFromMemoryResult(1),
        is(1)
    );
  }

  @Test
  void testLoadAddressMaximumAddress() {
    assertThat(
        loadFromMemoryResult(1 << MemoryFormat.ADDRESS_LENGTH - 1),
        is(1 << MemoryFormat.ADDRESS_LENGTH - 1)
    );
  }


  @Test
  void testLoadAddressBiggerNumber() {
    assertThat(
        loadFromMemoryResult(1 << MemoryFormat.ADDRESS_LENGTH),
        is(1 << MemoryFormat.ADDRESS_LENGTH)
    );
  }

  @Test
  void testLoadAddressMaximum() {
    assertThat(
        loadFromMemoryResult(MemoryFormat.VALUE_MAXIMUM),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }
  //</editor-fold>

  //<editor-fold desc="LDIV">
  @Test
  void testLoadAddressIndirectlyNegativeNumber() {
    assertThat(
        loadFromMemoryIndirectResult(-1),
        is(-1)
    );
  }

  @Test
  void testLoadAddressIndirectlyMinimum() {
    assertThat(
        loadFromMemoryIndirectResult(MemoryFormat.VALUE_MINIMUM),
        is(MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testLoadAddressIndirectlyUnderflowNoException() {
    assertThat(
        loadFromMemoryIndirectResult(MemoryFormat.VALUE_MINIMUM - 1),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }

  @Test
  void testLoadAddressIndirectlyZero() {
    assertThat(
        loadFromMemoryIndirectResult(0),
        is(0)
    );
  }

  @Test
  void testLoadAddressIndirectlyOne() {
    assertThat(
        loadFromMemoryIndirectResult(1),
        is(1)
    );
  }

  @Test
  void testLoadAddressIndirectlyMaximumAddress() {
    assertThat(
        loadFromMemoryIndirectResult(1 << MemoryFormat.ADDRESS_LENGTH - 1),
        is(1 << MemoryFormat.ADDRESS_LENGTH - 1)
    );
  }


  @Test
  void testLoadAddressIndirectlyBiggerNumber() {
    assertThat(
        loadFromMemoryIndirectResult(1 << MemoryFormat.ADDRESS_LENGTH),
        is(1 << MemoryFormat.ADDRESS_LENGTH)
    );
  }

  @Test
  void testLoadAddressIndirectlyMaximum() {
    assertThat(
        loadFromMemoryIndirectResult(MemoryFormat.VALUE_MAXIMUM),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }
  //</editor-fold>

  private int loadConstantResult(int constant) {
    return Load.LOAD_CONSTANT.apply(getState(), constant).registers().accumulator();
  }

  private int loadFromMemoryResult(int memory) {
    State state = getState().copy()
        .withMemory(
            MainMemory.create()
                .set(0, memory)
        );

    return Load.LOAD_FROM_ADDRESS.apply(state, 0).registers().accumulator();
  }

  private int loadFromMemoryIndirectResult(int memory) {
    State state = getState().copy()
        .withMemory(
            MainMemory.create()
                .set(0, 1)
                .set(1, memory)
        );

    return Load.LOAD_INDIRECT_FROM_ADDRESS.apply(state, 0).registers().accumulator();
  }
}