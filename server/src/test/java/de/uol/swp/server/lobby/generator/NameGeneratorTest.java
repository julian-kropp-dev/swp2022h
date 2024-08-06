package de.uol.swp.server.lobby.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** The TestNameGenerator class is responsible for testing the NameGenerator class. */
class TestNameGenerator {

  /**
   * Tests the generate() method of the NameGenerator class.
   *
   * <p>This test generates 10 names and ensures that they all match the pattern of four uppercase
   * characters and are unique. It also ensures that the generated names are added to the set of
   * used names.
   */
  @Test
  void testGenerate() {
    for (int i = 0; i < 10; i++) {
      String name = NameGenerator.generate();
      assertTrue(name.matches("^[A-Z]{4}$"));
      assertEquals(4, name.length());
      assertTrue(NameGenerator.generatedIDs.contains(name));
    }
  }

  /**
   * Tests the markAsUnused() method of the NameGenerator class.
   *
   * <p>This test generates a name, marks it as unused, and ensures that it is no longer in the set
   * of used names.
   */
  @Test
  void testMarkAsUnused() {
    String name = NameGenerator.generate();
    NameGenerator.markAsUnused(name);
    assertFalse(NameGenerator.generatedIDs.contains(name));
  }
}
