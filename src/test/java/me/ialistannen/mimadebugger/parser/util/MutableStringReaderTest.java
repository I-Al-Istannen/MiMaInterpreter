package me.ialistannen.mimadebugger.parser.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class MutableStringReaderTest {

  @Test
  void testCanReadEmpty() {
    assertThat(new MutableStringReader("").canRead()).isEqualTo(false);
  }

  @Test
  void testCanReadFinished() {
    MutableStringReader reader = new MutableStringReader("hello");
    assertThat(reader.canRead()).isEqualTo(true);

    reader.read("hello".length());

    assertThat(reader.canRead()).isEqualTo(false);
  }

  @Test
  void testPeekFirstChars() {
    MutableStringReader reader = new MutableStringReader("hello");
    assertThat(reader.peek(4)).isEqualTo("hello".substring(0, 4));
  }

  @Test
  void testPeekPattern() {
    MutableStringReader reader = new MutableStringReader("hello");
    assertThat(reader.peek(Pattern.compile("hel+"))).isEqualTo(true);
    assertThat(reader.peek(Pattern.compile("l+"))).isEqualTo(false);
  }

  @Test
  void testPeekString() {
    MutableStringReader reader = new MutableStringReader("hello you");
    assertThat(reader.peek("hello yo")).isEqualTo(true);
    assertThat(reader.peek("nope")).isEqualTo(false);
  }

  @Test
  void testPeekDoesNotMoveCursor() {
    MutableStringReader reader = new MutableStringReader("hello");
    reader.peek(4);
    reader.peek("hello");
    reader.peek(Pattern.compile("hello"));

    assertThat(reader.getCursor()).isEqualTo(0);
  }

  @Test
  void testReadNumberOfCharsMovesCursor() {
    MutableStringReader reader = new MutableStringReader("hello");
    reader.read(2);

    assertThat(reader.getCursor()).isEqualTo(2);
  }

  @Test
  void testReadPatternMovesCursor() {
    MutableStringReader reader = new MutableStringReader("hello");
    reader.read(Pattern.compile("hell"));

    assertThat(reader.getCursor()).isEqualTo(4);
  }

  @Test
  void testReadPatternIsEmptyStringIfNoMatch() {
    MutableStringReader reader = new MutableStringReader("hello");

    assertThat(reader.read(Pattern.compile("l+"))).isEqualTo("");
    assertThat(reader.getCursor()).isEqualTo(0);
  }

  @Test
  void testCopyIsUnaffectedByOriginal() {
    MutableStringReader reader = new MutableStringReader("hello");
    StringReader copy = reader.copy();

    reader.read(Pattern.compile("hell"));

    assertThat(reader.getCursor()).isEqualTo(4);
    assertThat(copy.getCursor()).isEqualTo(0);
  }

  @Test
  void testCopyHasSameContent() {
    MutableStringReader reader = new MutableStringReader("hello");
    StringReader copy = reader.copy();

    assertThat(copy.getString()).isEqualTo(reader.getString());
    assertThat(copy.getCursor()).isEqualTo(reader.getCursor());
    assertThat(copy.canRead()).isEqualTo(reader.canRead());
    assertThat(copy.canRead(20)).isEqualTo(reader.canRead(20));
  }

}
