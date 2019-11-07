package me.ialistannen.mimadebugger.util;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Objects;

/**
 * A fixed-size ring buffer.
 *
 * @param <E> the type of the elements in the buffer
 */
public class RingBuffer<E> extends AbstractQueue<E> {

  private E[] data;
  private int readPointer;
  private int writePointer;
  private int viewPointer;
  private int numElems;

  /**
   * Creates a new ring buffer with the given capacity.
   *
   * @param capacity the capacity
   */
  public RingBuffer(int capacity) {
    @SuppressWarnings("unchecked")
    E[] data = (E[]) new Object[capacity];
    this.data = data;
  }

  @Override
  public boolean offer(E e) {
    Objects.requireNonNull(e, "e can not be null!");

    data[writePointer] = e;
    viewPointer = writePointer;
    writePointer = wrap(writePointer + 1);
    numElems = Math.min(capacity(), numElems + 1);
    return true;
  }

  @Override
  public E poll() {
    if (isEmpty()) {
      return null;
    }

    E value = data[readPointer];

    data[readPointer] = null;

    readPointer = wrap(readPointer + 1);
    numElems--;

    return value;
  }

  @Override
  public E peek() {
    return data[readPointer];
  }

  @Override
  public int size() {
    return numElems;
  }

  /**
   * Returns the amount of elements this buffer can hold.
   *
   * @return the amount of elements this buffer can hold.
   */
  public int capacity() {
    return data.length;
  }

  /**
   * Checks whether there is a value before the view pointer.
   *
   * @return true if there is a value before the view pointer.
   */
  public boolean hasValueBeforeView() {
    int previousIndex = wrap(viewPointer - 1);
    boolean canGoBack = previousIndex != readPointer || previousIndex == writePointer;
    return data[previousIndex] != null && canGoBack;
  }

  /**
   * Checks whether there is a value at the view pointer.
   *
   * @return whether there is a value at the view pointer
   */
  public boolean hasValueAtView() {
    return getValueAtView() != null;
  }

  /**
   * Checks whether there is a value after the view pointer.
   *
   * @return whether there is a value at the view pointer
   */
  public boolean hasValueAfterView() {
    int nextIndex = wrap(viewPointer + 1);
    return data[nextIndex] != null && nextIndex != writePointer;
  }

  public void viewForwards() {
    viewPointer = wrap(viewPointer + 1);
  }

  public void viewBackwards() {
    viewPointer = wrap(viewPointer - 1);
  }

  /**
   * Returns the value at the view pointer.
   *
   * @return the value at the view pointer.
   */
  public E getValueAtView() {
    return data[viewPointer];
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      private int myReadPointer = readPointer;
      private int readCount;

      @Override
      public boolean hasNext() {
        return readCount < size();
      }

      @Override
      public E next() {
        E value = data[myReadPointer];
        myReadPointer = wrap(myReadPointer + 1);
        readCount++;
        return value;
      }
    };
  }

  private int wrap(int input) {
    if (input < 0) {
      input += data.length;
    }
    return input % data.length;
  }
}
