package me.ialistannen.mimadebugger.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RingBufferTest {

  private RingBuffer<Integer> buffer;
  private int bufferSize = 5;

  @BeforeEach
  void setUp() {
    buffer = new RingBuffer<>(bufferSize);
  }

  @Test
  void capacityIsCorrect() {
    assertEquals(
        bufferSize,
        buffer.capacity()
    );
  }

  @Test
  void initialSizeIsZero() {
    assertEquals(
        0,
        buffer.size()
    );
  }

  @Test
  void pushAndPopRoundTrip() {
    for (int i = 0; i < 2 * bufferSize; i++) {
      assertTrue(buffer.offer(i), "Adding may not never fail");

      assertEquals(
          (Integer) i,
          buffer.poll()
      );
    }
  }

  @Test
  void pushOverwrites() {
    for (int i = 0; i < bufferSize; i++) {
      buffer.offer(i);
      assertEquals(
          (Integer) 0,
          buffer.peek()
      );
    }
    buffer.offer(bufferSize);
    assertEquals(
        (Integer) bufferSize,
        buffer.poll()
    );
    for (int i = 1; i < bufferSize; i++) {
      assertEquals(
          (Integer) i,
          buffer.poll()
      );
    }
  }

  @Test
  void viewPointerWithOneElementCheck() {
    buffer.offer(20);
    assertFalse(buffer.hasValueBeforeView(), "Nothing before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertFalse(buffer.hasValueAfterView(), "Nothing after");

    assertEquals(
        (Integer) 20,
        buffer.getValueAtView()
    );
  }

  @Test
  void viewPointerWithMultipleElementCheck() {
    buffer.offer(1);
    buffer.offer(2);

    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertFalse(buffer.hasValueAfterView(), "Nothing after");

    assertEquals(
        (Integer) 2,
        buffer.getValueAtView()
    );
  }

  @Test
  void viewPointerMovementCheck() {
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);

    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertFalse(buffer.hasValueAfterView(), "Nothing after");
    assertEquals((Integer) 3, buffer.getValueAtView());

    buffer.viewBackwards();
    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertTrue(buffer.hasValueAfterView(), "Something after");
    assertEquals((Integer) 2, buffer.getValueAtView());

    buffer.viewBackwards();
    assertFalse(buffer.hasValueBeforeView(), "Nothing before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertTrue(buffer.hasValueAfterView(), "Something after");
    assertEquals((Integer) 1, buffer.getValueAtView());

    buffer.viewForwards();
    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertTrue(buffer.hasValueAfterView(), "Something after");
    assertEquals((Integer) 2, buffer.getValueAtView());

    buffer.viewForwards();
    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertFalse(buffer.hasValueAfterView(), "Nothing after");
    assertEquals((Integer) 3, buffer.getValueAtView());
  }

  @Test
  void viewPointerMovementCheckWithOverflow() {
    buffer = new RingBuffer<>(2);
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);

    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertFalse(buffer.hasValueAfterView(), "Nothing after");
    assertEquals((Integer) 3, buffer.getValueAtView());

    buffer.viewBackwards();
    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertTrue(buffer.hasValueAfterView(), "Something after");
    assertEquals((Integer) 2, buffer.getValueAtView());

    buffer.viewBackwards();
    assertFalse(buffer.hasValueBeforeView(), "Nothing before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertTrue(buffer.hasValueAfterView(), "Something after");
    assertEquals((Integer) 1, buffer.getValueAtView());

    buffer.viewForwards();
    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertTrue(buffer.hasValueAfterView(), "Something after");
    assertEquals((Integer) 2, buffer.getValueAtView());

    buffer.viewForwards();
    assertTrue(buffer.hasValueBeforeView(), "Something before");
    assertTrue(buffer.hasValueAtView(), "Something at");
    assertFalse(buffer.hasValueAfterView(), "Nothing after");
    assertEquals((Integer) 3, buffer.getValueAtView());
  }

}