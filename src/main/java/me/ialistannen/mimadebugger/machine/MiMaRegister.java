package me.ialistannen.mimadebugger.machine;

/**
 * A mima register.
 */
public enum MiMaRegister {
  INSTRUCTION_ADDRESS_REGISTER("IAR"),
  ACCUMULATOR("ACC"),
  STACK_POINTER("SP"),
  FRAME_POINTER("FP"),
  RETURN_ADDRESS_REGISTER("RA");

  private String abbreviation;

  MiMaRegister(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  public String getAbbreviation() {
    return abbreviation;
  }
}
