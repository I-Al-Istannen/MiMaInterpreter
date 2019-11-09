package me.ialistannen.mimadebugger.machine.memory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.exceptions.MemoryNotInitializedException;
import me.ialistannen.mimadebugger.exceptions.NumberOverflowException;
import me.ialistannen.mimadebugger.gui.state.MemoryValue;
import me.ialistannen.mimadebugger.parser.util.DoUnchecked;
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
   * @throws MemoryNotInitializedException if the memory is not yet initialized
   * @throws NumberOverflowException if the value does not fit into an address
   */
  public int get(int address) throws MemoryNotInitializedException, NumberOverflowException {
    int fixedLengthAddress = MemoryFormat.coerceToAddress(address);

    if (!data.containsKey(fixedLengthAddress)) {
      throw new MemoryNotInitializedException(fixedLengthAddress);
    }
    return data.get(fixedLengthAddress);
  }

  /**
   * Sets the value at the given address.
   *
   * @param address the address to write to
   * @param value the value to write
   * @return the resulting MainMemory object.
   * @throws MemoryNotInitializedException if the memory is not yet initialized
   * @throws NumberOverflowException if the value does not fit into an address
   */
  public MainMemory set(int address, int value) throws NumberOverflowException {
    int fixedLengthAddress = MemoryFormat.coerceToAddress(address);

    return new MainMemory(data.plus(fixedLengthAddress, MemoryFormat.coerceToValue(value)));
  }

  public Map<Integer, Integer> getMemory() {
    return data;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    data.entrySet().stream().sorted(Entry.comparingByKey())
        .forEach(entry ->
            stringBuilder.append(String.format(
                "%24s (%8d)  |  %24s (%4d %8d)",
                MemoryFormat.toString(entry.getKey(), 24, true),
                entry.getKey(),
                MemoryFormat.toString(entry.getValue(), 24, true),
                MemoryFormat.extractLargeOpcode(entry.getValue()),
                DoUnchecked.doIt(() -> MemoryFormat.extractArgument(entry.getValue()))
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

  /**
   * Creates a new MainMemory instance.
   *
   * @param values the values to store in it
   * @return the new MainMemory instance
   */
  public static MainMemory create(List<MemoryValue> values) {
    return new MainMemory(HashTreePMap.from(
        values.stream()
            .collect(Collectors.toMap(MemoryValue::address, MemoryValue::representation))
    ));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MainMemory memory = (MainMemory) o;
    return Objects.equals(data, memory.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }
}
