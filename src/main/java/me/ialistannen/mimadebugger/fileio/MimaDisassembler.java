package me.ialistannen.mimadebugger.fileio;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class MimaDisassembler {

  /**
   * Disassembles a given binary input.
   *
   * @param input the input
   * @param instructionSet the instruction set
   * @return the assembly code
   * @throws IllegalArgumentException if the input is malformed
   */
  public String fromAssembly(byte[] input, InstructionSet instructionSet)
      throws IllegalArgumentException {
    State state = new MimaBinaryFormat().load(input);

    List<Integer> memoryBytes = state.memory().getMemory().entrySet().stream()
        .sorted(Entry.comparingByKey())
        .map(Entry::getValue)
        .collect(toList());

    List<Optional<InstructionCall>> instructions = memoryBytes.stream()
        .map(encodedValue -> {
          Optional<InstructionCall> instructionCall = instructionSet.forEncodedValue(
              encodedValue
          );

          if (!instructionCall.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "Unknown opcode or invalid instruction: 0b%s (0x%s)",
                Integer.toHexString(encodedValue),
                MemoryFormat.toString(encodedValue, 24, false)
            ));
          }

          return instructionCall;
        })
        .collect(toList());

    return instructions.stream()
        .map(Optional::get)
        .map(call -> {
          // Treat all zeros as padding
          if (call.command().opcode() == 0 && call.argument() == 0) {
            return "0";
          }
          return call.command().name() + " " + call.argument();
        })
        .collect(Collectors.joining("\n"));
  }
}
