package me.ialistannen.mimadebugger.fileio;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.machine.MiMaRegister;
import me.ialistannen.mimadebugger.machine.State;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;

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

    String result = memoryBytes.stream()
        .map(encodedValue -> {
          Optional<InstructionCall> instructionCall = instructionSet.forEncodedValue(
              encodedValue
          );

          if (!instructionCall.isPresent()) {
            return String.valueOf(encodedValue.intValue());
          }

          return disassembleInstructionCall(instructionCall.get());
        })
        .collect(Collectors.joining("\n"));

    for (MiMaRegister value : MiMaRegister.values()) {
      result =
          ".reg " + value.getAbbreviation() + " " + value.get(state.registers()) + "\n" + result;
    }

    return result;
  }

  public String disassembleInstructionCall(InstructionCall call) {
    // Treat all zeros as padding
    if (call.command().opcode() == 0 && call.argument() == 0) {
      return "0";
    }
    if (!call.command().hasArgument() && call.argument() == 0) {
      return call.command().name();
    }
    return call.command().name() + " " + call.argument();
  }
}
