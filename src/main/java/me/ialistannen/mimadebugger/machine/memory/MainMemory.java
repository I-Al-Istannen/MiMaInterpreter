package me.ialistannen.mimadebugger.machine.memory;

import java.util.Map.Entry;
import me.ialistannen.mimadebugger.exceptions.MemoryNotInitializedException;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

/**
 * Represents the main memory of the machine.
 */
public class MainMemory {

  private PMap<Integer, Integer> data;

  private MainMemory(PMap<Integer, Integer> data) {
    this.data = data;
  }

  /**
   * Returns the value at a given memory address.
   *
   * @param address the address to read from
   * @return the value at this address. 0 if not initialized
   */
  public int get(int address) {
    if (!data.containsKey(address)) {
      throw new MemoryNotInitializedException(address);
    }
    return data.get(address);
  }

  /**
   * Sets the value at the given address.
   *
   * @param address the address to write to
   * @param value the value to write
   * @return the resulting MainMemory object.
   */
  public MainMemory set(int address, int value) {
    return new MainMemory(data.plus(address, value));
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    data.entrySet().stream().sorted(Entry.comparingByKey())
        .forEach(entry ->
            stringBuilder.append(String.format(
                "%24s (%8d)  |  %-24s (%8d)",
                MemoryFormat.toString(entry.getKey(), 24, true),
                entry.getKey(),
                MemoryFormat.toString(entry.getValue(), 24, true),
                entry.getValue()
            ))
                .append(System.lineSeparator())
        );

    return stringBuilder.toString();
  }

  /**
   * Creates a new MainMemory instance.
   *
   * @return the new MainMemory instance
   */
  public static MainMemory create() {
    return new MainMemory(HashTreePMap.empty());
  }

  public static void main(String[] args) {
    System.out.println(MainMemory.create()
        .set(20, 20)
        .set(50, 12)
        .set(30, -(int) (Math.pow(2, 23) - 0))
    );
  }
}
