package me.ialistannen.mimadebugger.machine.instructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Arithmetic;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Equality;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Functions;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Jump;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Load;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Logical;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Other;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Special;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Stack;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Store;
import me.ialistannen.mimadebugger.util.MemoryFormat;

/**
 * Contains all {@link Instruction}s the MiMa knows about.
 */
public class InstructionSet {

  private Map<Integer, Instruction> instructionMap;

  public InstructionSet() {
    this.instructionMap = new HashMap<>();

    Load.getInstructions().forEach(this::registerInstruction);
    Store.getInstructions().forEach(this::registerInstruction);
    Arithmetic.getInstructions().forEach(this::registerInstruction);
    Logical.getInstructions().forEach(this::registerInstruction);
    Equality.getInstructions().forEach(this::registerInstruction);
    Jump.getInstructions().forEach(this::registerInstruction);
    Special.getInstructions().forEach(this::registerInstruction);
    Other.getInstructions().forEach(this::registerInstruction);
    Stack.getInstructions().forEach(this::registerInstruction);
    Functions.getInstructions().forEach(this::registerInstruction);
  }

  /**
   * Registers a new {@link Instruction}.
   *
   * @param instruction the instruction to add
   * @throws IllegalArgumentException if the opcode is already registered
   */
  public void registerInstruction(Instruction instruction) {
    if (forName(instruction.name()).isPresent()) {
      throw new IllegalArgumentException("Name " + instruction.name() + " already registered!");
    }
    if (instructionMap.put(instruction.opcode(), instruction) != null) {
      throw new IllegalArgumentException("Opcode " + instruction.opcode() + " already registered!");
    }
  }

  /**
   * Returns the instruction for the given name.
   *
   * @param name the name of the instruction
   * @return the instruction, if found
   */
  public Optional<Instruction> forName(String name) {
    return instructionMap.values()
        .stream()
        .filter(instruction -> instruction.name().equalsIgnoreCase(name))
        .findFirst();
  }

  /**
   * Returns the instruction for the given opcode.
   *
   * @param opcode the opcode of the instruction
   * @return the instruction, if found
   */
  public Optional<Instruction> forOpcode(int opcode) {
    return Optional.ofNullable(instructionMap.get(opcode));
  }

  /**
   * Returns the {@link InstructionCall} for an encoded value.
   *
   * @param value the encoded value
   * @return the instruction call or an empty optional, if the opcode was not found
   */
  public Optional<InstructionCall> forEncodedValue(int value) {
    int opcode = MemoryFormat.extractOpcode(value);
    int argument = MemoryFormat.extractArgument(value);

    // check for small opcode first
    if (!instructionMap.containsKey(opcode)) {
      if (instructionMap.containsKey(MemoryFormat.extractLargeOpcode(value))) {
        opcode = MemoryFormat.extractLargeOpcode(value);
        argument = MemoryFormat.extractArgumentLargeOpcode(value);
      } else {
        return Optional.empty();
      }
    }

    Instruction instruction = instructionMap.get(opcode);

    return Optional.of(
        ImmutableInstructionCall.builder()
            .argument(argument)
            .command(instruction)
            .build()
    );
  }

  /**
   * Returns all registered instructions.
   *
   * @return all registered instructions
   */
  public List<Instruction> getAll() {
    return new ArrayList<>(instructionMap.values());
  }
}
