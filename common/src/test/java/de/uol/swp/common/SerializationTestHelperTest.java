package de.uol.swp.common;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import org.junit.jupiter.api.Test;

class SerializationTestHelperTest {

  @Test
  @SuppressWarnings("java:S5778")
  void checkNonSerializable() {
    assertThrows(
        RuntimeException.class,
        () ->
            SerializationTestHelper.checkSerializableAndDeserializable(
                new NotSerializable(), NotSerializable.class));
  }

  @Test
  void checkSerializable() {
    assertTrue(SerializationTestHelper.checkSerializableAndDeserializable("Hallo", String.class));
  }

  @SuppressWarnings({"unused", "InstantiatingAThreadWithDefaultRunMethod"})
  private static class NotSerializable implements Serializable {
    private final Thread thread = new Thread();
  }
}
