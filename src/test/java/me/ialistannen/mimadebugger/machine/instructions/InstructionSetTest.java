package me.ialistannen.mimadebugger.machine.instructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.mimadebugger.machine.instructions.defaultinstructions.Load;
import me.ialistannen.mimadebugger.util.MemoryFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstructionSetTest {

  private InstructionSet instructionSet;

  @BeforeEach
  void setup() {
    instructionSet = new InstructionSet();
  }

  @Test
  void testHasCorrectNumberOfInstructions() {
    assertThat(instructionSet.getAll().size()).isEqualTo(28);
  }

  @Test
  void addDuplicatedNameThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> instructionSet.registerInstruction(Load.LOAD_CONSTANT)
    );
  }

  @Test
  void addDuplicatedOpcodeThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> instructionSet.registerInstruction(
            ImmutableInstruction
                .copyOf(Load.LOAD_CONSTANT)
                .withName("hey")
        )
    );
  }

  @Test
  void getForOpcode() {
    assertThat(instructionSet.getAll().size()).isGreaterThan(1);

    for (Instruction instruction : instructionSet.getAll()) {
      assertThat(instructionSet.forOpcode(instruction.opcode())).contains(instruction);
    }
  }

  @Test
  void getForName() {
    assertThat(instructionSet.getAll().size()).isGreaterThan(1);

    for (Instruction instruction : instructionSet.getAll()) {
      assertThat(instructionSet.forName(instruction.name())).contains(instruction);
    }
  }

  @Test
  void testInstructionOpcodesAreUnique() {
    List<Integer> opcodes = instructionSet.getAll().stream()
        .map(Instruction::opcode)
        .collect(Collectors.toList());

    assertThat(opcodes.size()).isEqualTo(new HashSet<>(opcodes).size());
  }

  @Test
  void testInstructionNamesAreUnique() {
    List<String> opcodes = instructionSet.getAll().stream()
        .map(Instruction::name)
        .collect(Collectors.toList());

    assertThat(opcodes.size()).isEqualTo(new HashSet<>(opcodes).size());
  }

  @Test
  void testForEncodedValues() {
    for (Instruction instruction : instructionSet.getAll()) {
      ImmutableInstructionCall call = ImmutableInstructionCall.builder()
          .command(instruction)
          .argument(20)
          .build();

      assertThat(instructionSet.forEncodedValue(MemoryFormat.combineInstruction(call)))
          .contains(call);
    }
  }

  @Test
  void testForInvalidEncodedValue() {
    assertThat(instructionSet.forEncodedValue(0xFF0000)).isEqualTo(Optional.empty());
  }

}
