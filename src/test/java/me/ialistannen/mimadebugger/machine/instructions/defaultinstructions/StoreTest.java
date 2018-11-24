package me.ialistannen.mimadebugger.machine.instructions.defaultinstructions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.Test;

class StoreTest extends InstructionTest {

  //<editor-fold desc="STV">
  @Test
  void testStoreZero() {
    assertThat(
        storeMemoryValue(0, 20),
        is(0)
    );
  }

  @Test
  void testStoreOne() {
    assertThat(
        storeMemoryValue(1, 20),
        is(1)
    );
  }

  @Test
  void testStoreMinusOne() {
    assertThat(
        storeMemoryValue(-1, 20),
        is(-1)
    );
  }

  @Test
  void testStoreMinimum() {
    assertThat(
        storeMemoryValue(MemoryFormat.VALUE_MINIMUM, 20),
        is(MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testStoreMaximum() {
    assertThat(
        storeMemoryValue(MemoryFormat.VALUE_MAXIMUM, 20),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }

  @Test
  void testStoreAtNegativeAddress() {
    assertThrows(
        NumberOverflowException.class,
        () -> storeMemoryValue(MemoryFormat.VALUE_MAXIMUM, -1)
    );
  }

  @Test
  void testStoreTooBigNumber() {
    assertThat(
        storeMemoryValue(MemoryFormat.VALUE_MAXIMUM + 1, 20),
        is(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + 1))
    );
  }
  //</editor-fold>

  //<editor-fold desc="STIV">
  @Test
  void testStoreIndirectlyZero() {
    assertThat(
        storeMemoryValueIndirect(0, 2, 20),
        is(0)
    );
  }

  @Test
  void testStoreIndirectlyOne() {
    assertThat(
        storeMemoryValueIndirect(1, 2, 20),
        is(1)
    );
  }

  @Test
  void testStoreIndirectlyMinusOne() {
    assertThat(
        storeMemoryValueIndirect(-1, 2, 20),
        is(-1)
    );
  }

  @Test
  void testStoreIndirectlyMinimum() {
    assertThat(
        storeMemoryValueIndirect(MemoryFormat.VALUE_MINIMUM, 2, 20),
        is(MemoryFormat.VALUE_MINIMUM)
    );
  }

  @Test
  void testStoreIndirectlyMaximum() {
    assertThat(
        storeMemoryValueIndirect(MemoryFormat.VALUE_MAXIMUM, 2, 20),
        is(MemoryFormat.VALUE_MAXIMUM)
    );
  }

  @Test
  void testStoreIndirectlyAtNegativeAddress() {
    assertThrows(
        NumberOverflowException.class,
        () -> storeMemoryValueIndirect(MemoryFormat.VALUE_MAXIMUM, 2, -1)
    );
  }

  @Test
  void testStoreIndirectlyAtNegativeForwardAddress() {
    assertThrows(
        NumberOverflowException.class,
        () -> storeMemoryValueIndirect(MemoryFormat.VALUE_MAXIMUM, -2, 2)
    );
  }

  @Test
  void testStoreIndirectlyTooBigNumber() {
    assertThat(
        storeMemoryValueIndirect(MemoryFormat.VALUE_MAXIMUM + 1, 2, 20),
        is(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + 1))
    );
  }
  //</editor-fold>

  private int storeMemoryValue(int accumulator, int storeAt) {
    State state = getState().copy()
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        );
    return Store.STORE.apply(state, storeAt).memory().get(storeAt);
  }

  private int storeMemoryValueIndirect(int accumulator, int forward, int storeAt) {
    State state = getState().copy()
        .withMemory(
            MainMemory.create()
                .set(forward, storeAt)
        )
        .withRegisters(
            ImmutableRegisters.builder()
                .accumulator(accumulator)
                .build()
        );
    return Store.STORE_INDIRECT.apply(state, forward).memory().get(storeAt);
  }
}