package me.ialistannen.mimadebugger.machine.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import me.ialistannen.mimadebugger.exceptions.MemoryNotInitializedException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainMemoryTest {

  private MainMemory memory;

  @BeforeEach
  void setup() {
    this.memory = MainMemory.create();
  }

  @Test
  void testSetValue() throws NumberOverflowException, MemoryNotInitializedException {
    testSetValue(getRandomAddress(), 32);
  }

  @Test
  void testSetAddressOutsideRange() {
    assertThrows(
        NumberOverflowException.class,
        () -> memory.set(1 << MemoryFormat.ADDRESS_LENGTH, 20)
    );
  }

  @Test
  void testSetToNegativeAddress() {
    assertThrows(
        NumberOverflowException.class,
        () -> memory.set(-1, 20)
    );
  }

  @Test
  void testSetMaximumValue() throws NumberOverflowException, MemoryNotInitializedException {
    testSetValue(getRandomAddress(), MemoryFormat.VALUE_MAXIMUM);
  }

  @Test
  void testSetTooLargeValue() throws NumberOverflowException, MemoryNotInitializedException {
    assertThat(memory.set(1, MemoryFormat.VALUE_MAXIMUM + 1).get(1))
        .isEqualTo(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + 1));
  }

  @Test
  void testSetNegativeValue() throws NumberOverflowException, MemoryNotInitializedException {
    assertThat(memory.set(1, MemoryFormat.VALUE_MINIMUM).get(1))
        .isEqualTo(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MINIMUM));
  }

  @Test
  void testSetDoesNotMutateOriginalUnset()
      throws NumberOverflowException, MemoryNotInitializedException {
    MainMemory setMemory = memory.set(0, 32);
    assertThat(setMemory.get(0))
        .isEqualTo(32);

    assertThrows(
        MemoryNotInitializedException.class,
        () -> memory.get(0)
    );
  }

  @Test
  void testSetDoesNotMutateOriginalSetToSameSlot()
      throws NumberOverflowException, MemoryNotInitializedException {
    int originalValue = 30;
    memory = memory.set(0, originalValue);
    int changedValue = 32;

    MainMemory modifiedMemory = memory.set(0, changedValue);

    assertThat(modifiedMemory.get(0))
        .isEqualTo(changedValue);
    assertThat(memory.get(0))
        .isEqualTo(originalValue);
  }

  @Test
  void testGetMemory() throws NumberOverflowException {
    Map<Integer, Integer> map = new HashMap<>();
    map.put(2, 21);
    map.put(5, 30);
    map.put(100, -10);
    assertThat(memory.set(2, 20).set(5, 30).set(2, 21).set(100, -10).getMemory())
        .isEqualTo(map);
  }

  @Test
  void testHashcodeSimple() throws NumberOverflowException {
    MainMemory first = MainMemory.create()
        .set(0, 20);
    MainMemory second = MainMemory.create()
        .set(0, 20);

    assertThat(first.hashCode())
        .isEqualTo(second.hashCode());
  }

  @Test
  void testEqualsNull() {
    assertThat(memory).isNotNull();
  }

  @Test
  void verifyToString() throws NumberOverflowException {
    MainMemory memory = MainMemory.create()
        .set(0, 20);

    assertThat(memory.toString())
        .isEqualTo(
            "                       0 (       0)  |                    010100 (   0       20)\n");
  }

  private void testSetValue(int address, int value)
      throws NumberOverflowException, MemoryNotInitializedException {
    assertThat(memory.set(address, value).get(address))
        .isEqualTo(value);
  }

  private int getRandomAddress() {
    return ThreadLocalRandom.current().nextInt(1 << MemoryFormat.ADDRESS_LENGTH);
  }
}
