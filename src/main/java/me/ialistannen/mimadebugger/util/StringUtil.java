package me.ialistannen.mimadebugger.util;

public class StringUtil {

  public static String repeat(String input, int amount) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < amount; i++) {
      builder.append(input);
    }

    return builder.toString();
  }
}
