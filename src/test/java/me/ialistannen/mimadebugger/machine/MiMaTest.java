package me.ialistannen.mimadebugger.machine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.ialistannen.mimadebugger.exceptions.InstructionNotFoundException;
import me.ialistannen.mimadebugger.exceptions.ProgramHaltException;
import me.ialistannen.mimadebugger.machine.instructions.ImmutableInstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.Instruction;
import me.ialistannen.mimadebugger.machine.instructions.InstructionCall;
import me.ialistannen.mimadebugger.machine.instructions.InstructionSet;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Arithmetic;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Load;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Special;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Store;
import me.ialistannen.mimadebugger.machine.memory.ImmutableRegisters;
import me.ialistannen.mimadebugger.machine.memory.MainMemory;
import me.ialistannen.mimadebugger.machine.memory.Registers;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiMaTest {

  private MiMa miMa;
  private State initialState;
  private MainMemory memory;

  @BeforeEach
  void setup() {
    InstructionSet instructionSet = new InstructionSet();

    memory = MainMemory.create()
        .set(
            0, MemoryFormat.combineInstruction(toCall(Load.LOAD_CONSTANT, 20))
        )
        .set(
            1, MemoryFormat.combineInstruction(toCall(Store.STORE, 20))
        )
        .set(
            2, MemoryFormat.combineInstruction(toCall(Arithmetic.ADD, 20))
        )
        .set(
            3, MemoryFormat.combineInstruction(toCall(Special.HALT, 0))
        );
    initialState = ImmutableState.builder()
        .registers(ImmutableRegisters.builder().build())
        .memory(memory)
        .build();
    miMa = new MiMa(initialState, instructionSet);
  }

  @Test
  void testCopy() {
    miMa.step();
    assertThat(
        miMa.copy(initialState).getCurrentState(),
        is(initialState)
    );
    assertThat(
        miMa.getCurrentState(),
        is(not(initialState))
    );
  }

  @Test
  void testRegistersUpdatedAfterStep() {
    assertThat(
        miMa.step(),
        is(miMa.getCurrentState())
    );

    Registers registers = miMa.getCurrentState().registers();

    assertThat(
        registers.accumulator(),
        is(20)
    );
    assertThat(
        registers.instructionPointer(),
        is(1)
    );
    // No new instruction is in the register yet
    assertThat(
        registers.instruction(),
        is(MemoryFormat.combineInstruction(toCall(Load.LOAD_CONSTANT, 20)))
    );
  }

  @Test
  void testThrowsHaltExceptionWhenFinished() {
    miMa.step();
    miMa.step();
    miMa.step();

    assertThrows(
        ProgramHaltException.class,
        () -> miMa.step()
    );
  }

  @Test
  void stepToIllegalOpcodeErrors() {
    miMa = miMa.copy(
        ImmutableState.builder()
            .registers(ImmutableRegisters.builder().build())
            .memory(MainMemory.create().set(0, 0b00000000111000000000000000000000))
            .build()
    );

    assertThrows(
        InstructionNotFoundException.class,
        () -> miMa.step()
    );
  }

  private InstructionCall toCall(Instruction instruction, int argument) {
    return ImmutableInstructionCall.builder()
        .command(instruction)
        .argument(argument)
        .build();
  }

}