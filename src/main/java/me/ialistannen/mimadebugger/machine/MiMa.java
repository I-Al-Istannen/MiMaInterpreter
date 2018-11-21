package me.ialistannen.mimadebugger.machine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.machine.program.ProgramParser;
import me.ialistannen.mimadebugger.util.MemoryFormat;

public class MiMa {

  private State currentState;
  private InstructionSet instructionSet;

  /**
   * Creates a new MiMa.
   *
   * @param initialState the initial state
   * @param instructionSet the {@link InstructionSet} to use.
   */
  public MiMa(State initialState, InstructionSet instructionSet) {
    this.currentState = initialState;
    this.instructionSet = instructionSet;
  }

  /**
   * Performs a single calculation step.
   *
   * @return the new state
   */
  public State step() {
    int currentInstruction = currentState.registers().instruction();

    int opcode = MemoryFormat.extractOpcode(currentInstruction);
    int argument = MemoryFormat.extractArgument(currentInstruction);

    Instruction instruction = instructionSet.forOpcode(opcode)
        .orElseThrow(() -> new InstructionNotFoundException(opcode));

    // TODO: Make nicer
    if (instruction.name().equalsIgnoreCase("HALT")) {
      throw new RuntimeException("Program exit!");
    }

    currentState = fetchNextInstruction();

    return currentState = instruction.apply(currentState, argument);
  }

  private State fetchNextInstruction() {
    return currentState.copy()
        .withRegisters(
            currentState.registers().copy()
                .withInstruction(
                    currentState.memory().get(currentState.registers().instructionPointer())
                )
                .withInstructionPointer(currentState.registers().instructionPointer() + 1)
        );
  }

  /**
   * Returns the current {@link State} of the MiMa.
   *
   * @return the current {@link State} of the MiMa
   */
  public State getCurrentState() {
    return currentState;
  }


  public static void main(String[] args) {
    InstructionSet instructionSet = new InstructionSet();

    ProgramParser parser = new ProgramParser(instructionSet);
    List<InstructionCall> calls = parser.parseFromNames(readResource("/AddOne.mima"));

    MainMemory memory = MainMemory.create();

    for (int i = 0; i < calls.size(); i++) {
      InstructionCall call = calls.get(i);
      int combineInstruction = MemoryFormat
          .combineInstruction(call.command().opcode(), call.argument());
      memory = memory.set(i, combineInstruction);
    }

    ImmutableState state = ImmutableState.builder()
        .memory(memory)
        .registers(
            ImmutableRegisters.builder()
                .instruction(memory.get(0))
                .instructionPointer(1)
                .build()
        )
        .build();

    MiMa miMa = new MiMa(state, instructionSet);
    while (true) {
      State currentState = miMa.getCurrentState();

      System.out.println("Current instruction address: " + currentState.registers().instructionPointer());

      int opcode = MemoryFormat.extractOpcode(currentState.registers().instruction());
      Optional<Instruction> instruction = instructionSet.forOpcode(opcode);
      System.out.println("Executing: " + instruction.map(it -> it.name() + ": " + it.opcode()));
      State step = miMa.step();
      System.out.println(step.registers());

      System.out.println();
    }
  }

  private static List<String> readResource(String path) {
    try (InputStream inputStream = MiMa.class.getResourceAsStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader)) {

      return reader.lines().collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
