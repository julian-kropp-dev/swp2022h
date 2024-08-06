package de.uol.swp.common.game.floor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.uol.swp.common.game.floor.operation.Pit;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/** Tests the functionality of the floor plan class. */
class FloorTest {

  FloorPlan defaultFloor =
      new FloorPlan(Paths.get("src/main/java/de/uol/swp/common/game/floor/plans/testPlan.csv"));

  @Test
  void fileTestFail() {
    Path path = Paths.get("fail");
    assertThrows(IllegalStateException.class, () -> new FloorPlan(path));
  }

  @Test
  void fileTestPass() {
    for (int i = 0; i < 5; i++) {
      assertNotNull(defaultFloor.getStart());
    }
    FloorField start1 = defaultFloor.getStart();
    FloorField start2 = defaultFloor.getStart();
    assertNotEquals(start1, start2);
    assertEquals("49", start1.getNeighbour(Direction.NORTH).getBasicFloorField().getId());
    assertEquals(
        "49",
        start2
            .getNeighbour(Direction.NORTH)
            .getNeighbour(Direction.NORTH)
            .getBasicFloorField()
            .getId());
    assertEquals(6, start1.getY());
    assertEquals(4, start1.getNeighbour(Direction.NORTH).getX());
  }

  @Test
  void stringTestFail() {
    assertThrows(IllegalArgumentException.class, () -> new FloorPlan("049;053;0L3\n049;053;0L3"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new FloorPlan(
                "500;253;149;049;053;049;049;053;049;049;053;049\n"
                    + "149;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049"));
  }

  @Test
  void checkpointTest() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new FloorPlan(
                "096;253;1491;049;053;049;049;053;049;049;053;049\n"
                    + "149;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049"));

    assertDoesNotThrow(
        () ->
            new FloorPlan(
                "096;253;1681;1692;053;049;049;053;049;049;053;049\n"
                    + "149;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                    + "049;053;049;049;053;049;049;053;049;049;053;049"));
  }

  @Test
  void addStartArea() {

    String string =
        "096;253;1681;1692;053;049;049;053;049;049;053;049\n"
            + "149;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049\n"
            + "049;053;049;049;053;049;049;053;049;049;053;049";

    FloorPlan plan = new FloorPlan(string);

    String[] array = string.split("\\R");
    String[][] matrixWithoutStart = new String[array.length][];
    for (int i = 0; i < array.length; i++) {
      matrixWithoutStart[i] = array[i].split(";");
    }
    String[][] result = new String[0][];
    try {
      Method methode = FloorPlan.class.getDeclaredMethod("addStartArea", String[][].class);
      methode.setAccessible(true);

      result = (String[][]) methode.invoke(plan, (Object) matrixWithoutStart);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      e.printStackTrace();
      assert false;
    }

    assertEquals(16, result[1].length);
  }

  @SuppressWarnings("UnusedAssignment")
  @Test
  void stringTurnedTestPass() {
    FloorPlan floorPlan =
        new FloorPlan(
            "096;253;149;049;053;049;049;053;049;049;053;049\n"
                + "149;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049\n"
                + "049;053;049;049;053;049;049;053;049;049;053;049");
    FloorField start = floorPlan.getStart();
    start = floorPlan.getStart();
    assertEquals("49", start.getNeighbour(Direction.SOUTH).getBasicFloorField().getId());
    assertEquals(4, start.getX());
    assertEquals(0, start.getY());
    for (int i = 0; i < 7; i++) {
      assertNotNull(floorPlan.getStart());
    }
    assertNull(floorPlan.getStart());
  }

  @Test
  void edgeTest() {
    FloorField start = defaultFloor.getStart();
    assertEquals(
        Pit.class,
        start
            .getNeighbour(Direction.WEST)
            .getNeighbour(Direction.WEST)
            .getBasicFloorField()
            .getOperations()
            .get(-1)
            .getClass());
    assertNull(
        start
            .getNeighbour(Direction.WEST)
            .getNeighbour(Direction.WEST)
            .getNeighbour(Direction.WEST)
            .getBasicFloorField()
            .getOperations()
            .get(-1));
  }
}
