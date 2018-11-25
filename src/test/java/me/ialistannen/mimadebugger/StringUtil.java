package me.ialistannen.mimadebugger;

public class StringUtil {

  /**
   * Repeats the given string "amount" times.
   *
   * @param input the input to repeat
   * @param amount how often to repeat it
   * @return the repeated string
   */
  public static String repeat(String input, int amount) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < amount; i++) {
      builder.append(input);
    }

    return builder.toString();
  }
}
