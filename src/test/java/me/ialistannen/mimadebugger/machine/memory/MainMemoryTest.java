package me.ialistannen.mimadebugger.machine.memory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
  void testSetValue() {
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
  void testSetMaximumValue() {
    testSetValue(getRandomAddress(), MemoryFormat.VALUE_MAXIMUM);
  }

  @Test
  void testSetTooLargeValue() {
    assertThat(
        memory.set(1, MemoryFormat.VALUE_MAXIMUM + 1).get(1),
        is(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MAXIMUM + 1))
    );
  }

  @Test
  void testSetNegativeValue() {
    assertThat(
        memory.set(1, MemoryFormat.VALUE_MINIMUM).get(1),
        is(MemoryFormat.coerceToValue(MemoryFormat.VALUE_MINIMUM))
    );
  }

  @Test
  void testSetDoesNotMutateOriginalUnset() {
    MainMemory setMemory = memory.set(0, 32);
    assertThat(
        setMemory.get(0),
        is(32)
    );

    assertThrows(
        MemoryNotInitializedException.class,
        () -> memory.get(0)
    );
  }

  @Test
  void testSetDoesNotMutateOriginalSetToSameSlot() {
    int originalValue = 30;
    memory = memory.set(0, originalValue);
    int changedValue = 32;

    MainMemory modifiedMemory = memory.set(0, changedValue);

    assertThat(
        modifiedMemory.get(0),
        is(changedValue)
    );
    assertThat(
        memory.get(0),
        is(originalValue)
    );
  }

  @Test
  void testGetMemory() {
    Map<Integer, Integer> map = new HashMap<>();
    map.put(2, 21);
    map.put(5, 30);
    map.put(100, -10);
    assertThat(
        memory.set(2, 20).set(5, 30).set(2, 21).set(100, -10).getMemory(),
        is(map)
    );
  }

  @Test
  void testHashcodeSimple() {
    MainMemory first = MainMemory.create()
        .set(0, 20);
    MainMemory second = MainMemory.create()
        .set(0, 20);

    assertThat(
        first.hashCode(),
        is(second.hashCode())
    );
  }

  @Test
  void testEqualsNull() {
    assertThat(
        memory.equals(null),
        is(false)
    );
  }

  @Test
  void verifyToString() {
    MainMemory memory = MainMemory.create()
        .set(0, 20);

    assertThat(
        memory.toString(),
        is("                       0 (       0)  |                    010100 (   0       20)\n")
    );
  }

  private void testSetValue(int address, int value) {
    assertThat(
        memory.set(address, value).get(address),
        is(value)
    );
  }

  private int getRandomAddress() {
    return ThreadLocalRandom.current().nextInt(1 << MemoryFormat.ADDRESS_LENGTH);
  }
}