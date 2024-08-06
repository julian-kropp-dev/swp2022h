package de.uol.swp.common.game.floor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Tests the functionality of the directions' enumeration. */
class DirectionTest {

  @Test
  void rotateTest() {
    assertEquals(Direction.SOUTH, Direction.NORTH.rotate(Direction.SOUTH));
    assertEquals(Direction.EAST, Direction.SOUTH.rotate(Direction.WEST));
  }

  @Test
  void getTest() {
    assertEquals(Direction.EAST, Direction.get(1));
    assertNull(Direction.get(4));
  }
}
