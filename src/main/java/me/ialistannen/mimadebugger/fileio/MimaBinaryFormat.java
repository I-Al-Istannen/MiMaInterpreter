package me.ialistannen.mimadebugger.fileio;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import me.ialistannen.mimadebugger.machine.ImmutableState;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.machine.memory.Registers;

/**
 * A writer and reader for the MiMa binary format.
 */
public class MimaBinaryFormat {

  /**
   * Saves the state to a byte array.
   *
   * @param state the state to save
   * @return the resulting byte array
   */
  public byte[] save(State state) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // TODO: Implement other registers
    writeInt(out, state.registers().instructionPointer());
    writeInt(out, state.registers().accumulator());
    writeInt(out, 0); // RA
    writeInt(out, 0); // SP
    writeInt(out, 0); // FP

    Map<Integer, Integer> memory = state.memory().getMemory();
    int maxAddress = memory.keySet().stream()
        .mapToInt(Integer::intValue)
        .max()
        .orElse(0);

    for (int i = 0; i <= maxAddress; i++) {
      int value = memory.getOrDefault(i, 0);
      writeInt(out, value);
    }

    return out.toByteArray();
  }

  private void writeInt(ByteArrayOutputStream out, int value) {
    out.write(value >>> 16 & 0xFF);
    out.write(value >>> 8 & 0xFF);
    out.write(value & 0xFF);
  }

  /**
   * Loads a memory dump back to a state.
   *
   * @param data the data to read
   * @return the loaded state
   */
  public State load(byte[] data) {
    if (data.length % 3 != 0) {
      throw new IllegalArgumentException("Invalid memory dump, not a multiple of 3");
    }
    // TODO: Implement other registers
    Registers registers = ImmutableRegisters.builder()
        .instructionPointer(readInt(data, 0))
        .accumulator(readInt(data, 3))
        .build();

    MainMemory memory = MainMemory.create();
    int memoryDumpOffset = 5 * 3;

    for (int i = 0; i < data.length / 3 - 5; i++) {
      int baseOffset = i * 3 + memoryDumpOffset;
      int instruction = readInt(data, baseOffset);
      memory = memory.set(i, instruction);
    }

    return ImmutableState.builder()
        .memory(memory)
        .registers(registers)
        .build();
  }

  private int readInt(byte[] data, int baseOffset) {
    return (data[baseOffset] & 0xFF) << 16
        | (data[baseOffset + 1] & 0xFF) << 8
        | data[baseOffset + 2] & 0xFF;
  }
}
