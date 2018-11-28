package me.ialistannen.mimadebugger.parser.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class MutableStringReaderTest {

  @Test
  void testCanReadEmpty() {
    assertThat(
        new MutableStringReader("").canRead(),
        is(false)
    );
  }

  @Test
  void testCanReadFinished() {
    MutableStringReader reader = new MutableStringReader("hello");
    assertThat(
        reader.canRead(),
        is(true)
    );

    reader.read("hello".length());

    assertThat(
        reader.canRead(),
        is(false)
    );
  }

  @Test
  void testPeekFirstChars() {
    MutableStringReader reader = new MutableStringReader("hello");
    assertThat(
        reader.peek(4),
        is("hello".substring(0, 4))
    );
  }

  @Test
  void testPeekPattern() {
    MutableStringReader reader = new MutableStringReader("hello");
    assertThat(
        reader.peek(Pattern.compile("hel+")),
        is(true)
    );
    assertThat(
        reader.peek(Pattern.compile("l+")),
        is(false)
    );
  }

  @Test
  void testPeekString() {
    MutableStringReader reader = new MutableStringReader("hello you");
    assertThat(
        reader.peek("hello yo"),
        is(true)
    );
    assertThat(
        reader.peek("nope"),
        is(false)
    );
  }

  @Test
  void testPeekDoesNotMoveCursor() {
    MutableStringReader reader = new MutableStringReader("hello");
    reader.peek(4);
    reader.peek("hello");
    reader.peek(Pattern.compile("hello"));

    assertThat(
        reader.getCursor(),
        is(0)
    );
  }

  @Test
  void testReadNumberOfCharsMovesCursor() {
    MutableStringReader reader = new MutableStringReader("hello");
    reader.read(2);

    assertThat(
        reader.getCursor(),
        is(2)
    );
  }

  @Test
  void testReadPatternMovesCursor() {
    MutableStringReader reader = new MutableStringReader("hello");
    reader.read(Pattern.compile("hell"));

    assertThat(
        reader.getCursor(),
        is(4)
    );
  }

  @Test
  void testReadPatternIsEmptyStringIfNoMatch() {
    MutableStringReader reader = new MutableStringReader("hello");

    assertThat(
        reader.read(Pattern.compile("l+")),
        is("")
    );
    assertThat(
        reader.getCursor(),
        is(0)
    );
  }

  @Test
  void testCopyIsUnaffectedByOriginal() {
    MutableStringReader reader = new MutableStringReader("hello");
    StringReader copy = reader.copy();

    reader.read(Pattern.compile("hell"));

    assertThat(
        reader.getCursor(),
        is(4)
    );
    assertThat(
        copy.getCursor(),
        is(0)
    );
  }

  @Test
  void testCopyHasSameContent() {
    MutableStringReader reader = new MutableStringReader("hello");
    StringReader copy = reader.copy();

    assertThat(
        copy.getString(),
        is(reader.getString())
    );
    assertThat(
        copy.getCursor(),
        is(reader.getCursor())
    );
    assertThat(
        copy.canRead(),
        is(reader.canRead())
    );
    assertThat(
        copy.canRead(20),
        is(reader.canRead(20))
    );
  }

}