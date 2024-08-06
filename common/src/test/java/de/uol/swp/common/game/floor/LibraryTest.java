package de.uol.swp.common.game.floor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Tests the functionality of the library class. */
class LibraryTest {

  @Test
  @SuppressWarnings("java:S5961")
  void getTest() {
    assertNull(Library.get("549"));
    assertEquals("49", Objects.requireNonNull(Library.get("49")).getId());

    BasicFloorField p0 = Library.get("33");
    assertNotNull(p0);
    assertEquals("33", p0.getId());
    assertEquals(3, p0.getPossibleNeighbours().size());
    assertEquals(1, p0.getOperations().size());

    BasicFloorField p1 = Library.get("32");
    assertNotNull(p1);
    assertEquals("32", p1.getId());
    assertEquals(3, p1.getPossibleNeighbours().size());
    assertEquals(1, p1.getOperations().size());

    BasicFloorField p2 = Library.get("35");
    assertNotNull(p2);
    assertEquals("35", p2.getId());
    assertEquals(3, p2.getPossibleNeighbours().size());
    assertEquals(1, p2.getOperations().size());

    BasicFloorField p3 = Library.get("34");
    assertNotNull(p3);
    assertEquals("34", p3.getId());
    assertEquals(3, p3.getPossibleNeighbours().size());
    assertEquals(1, p3.getOperations().size());

    BasicFloorField p4 = Library.get("36");
    assertNotNull(p4);
    assertEquals("36", p4.getId());
    assertEquals(3, p4.getPossibleNeighbours().size());
    assertEquals(1, p4.getOperations().size());

    BasicFloorField nn = Library.get("00");
    assertNotNull(nn);
    assertEquals("00", nn.getId());
    assertEquals(4, nn.getPossibleNeighbours().size());
    assertEquals(0, nn.getOperations().size());

    BasicFloorField pr15 = Library.get("39");
    assertNotNull(pr15);
    assertEquals("39", pr15.getId());
    assertEquals(4, pr15.getPossibleNeighbours().size());
    assertEquals(2, pr15.getOperations().size());

    BasicFloorField pr24 = Library.get("40");
    assertNotNull(pr24);
    assertEquals("40", pr24.getId());
    assertEquals(4, pr24.getPossibleNeighbours().size());
    assertEquals(2, pr24.getOperations().size());

    BasicFloorField pr2 = Library.get("42");
    assertNotNull(pr2);
    assertEquals("42", pr2.getId());
    assertEquals(4, pr2.getPossibleNeighbours().size());
    assertEquals(2, pr2.getOperations().size());

    BasicFloorField pr3 = Library.get("41");
    assertNotNull(pr3);
    assertEquals("41", pr3.getId());
    assertEquals(4, pr3.getPossibleNeighbours().size());
    assertEquals(2, pr3.getOperations().size());

    // Test getting a non-existent basic floor field
    BasicFloorField field = Library.get("non-existent");
    assertNull(field);
  }
}
