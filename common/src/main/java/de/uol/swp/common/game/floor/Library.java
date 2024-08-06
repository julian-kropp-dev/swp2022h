package de.uol.swp.common.game.floor;

import de.uol.swp.common.game.floor.operation.Belt;
import de.uol.swp.common.game.floor.operation.Checkpoint;
import de.uol.swp.common.game.floor.operation.ExpressBelt;
import de.uol.swp.common.game.floor.operation.Gear;
import de.uol.swp.common.game.floor.operation.Laser;
import de.uol.swp.common.game.floor.operation.Pit;
import de.uol.swp.common.game.floor.operation.Press;
import de.uol.swp.common.game.floor.operation.Pusher;
import de.uol.swp.common.game.floor.operation.Repair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

/** The library holds the data for all basic floor fields and creates them. */
@Singleton
public class Library {

  private static final Map<String, BasicFloorField> map = new HashMap<>();

  static {
    new FieldBuilder("96") // Start field
        .setNb(Direction.NORTH, Direction.SOUTH)
        .build();
    new FieldBuilder("97") // Start field for Tests
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH)
        .build();

    // Basic Fields
    new FieldBuilder("00")
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .build();
    new FieldBuilder("01").setNb(Direction.EAST, Direction.SOUTH, Direction.WEST).build();
    new FieldBuilder("02").setNb(Direction.SOUTH, Direction.WEST).build();

    // Pit
    new FieldBuilder("64")
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.EAST, Direction.SOUTH, Direction.WEST)
        .build();
    new FieldBuilder("65")
        .setNb(Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.EAST, Direction.SOUTH, Direction.WEST)
        .build();
    new FieldBuilder("70")
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .build();
    new FieldBuilder("71")
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .build();
    new FieldBuilder("72")
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(
            new Laser(2, false), Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH)
        .build();
    new FieldBuilder("73")
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .build();
    new FieldBuilder("98")
        .setNb(Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Laser(1, false), Direction.SOUTH)
        .build();
    new FieldBuilder("99")
        .setNb(Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Pit(), Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Laser(1, false), Direction.NORTH)
        .build();

    // Pusher
    new FieldBuilder("33") // Pusher stick on the SOUTH wall
        .setNb(Direction.NORTH, Direction.WEST, Direction.EAST)
        .setOp(new Pusher(new Integer[] {1, 3, 5}), Direction.SOUTH)
        .build();
    new FieldBuilder("32") // Pusher stick on the SOUTH wall
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new Pusher(1), Direction.SOUTH)
        .build();
    new FieldBuilder("35") // Pusher stick on the SOUTH wall
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new Pusher(new Integer[] {2, 4}), Direction.SOUTH)
        .build();
    new FieldBuilder("34") // Pusher stick on the SOUTH wall
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new Pusher(2), Direction.SOUTH)
        .build();
    new FieldBuilder("36") // Pusher stick on the SOUTH wall
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new Pusher(3), Direction.SOUTH)
        .build();

    // Laser
    new FieldBuilder("48") // Laser stick on the SOUTH wall single laser
        .setNb(Direction.NORTH, Direction.WEST, Direction.EAST)
        .setOp(new Laser(1, true), Direction.SOUTH)
        .build();
    new FieldBuilder("49") // Laser stick on the SOUTH wall double laser
        .setNb(Direction.NORTH, Direction.WEST, Direction.EAST)
        .setOp(new Laser(2, true), Direction.SOUTH)
        .build();
    new FieldBuilder("53") // Laser stick on no wall single laser
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .setOp(new Laser(1, false), Direction.SOUTH, Direction.NORTH)
        .build();
    new FieldBuilder("54") // Laser stick on no wall double laser
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .setOp(new Laser(2, false), Direction.SOUTH, Direction.NORTH)
        .build();
    new FieldBuilder("52") // Laser stick on SOUTH and NORTH is a Wall
        .setNb(Direction.EAST, Direction.WEST)
        .setOp(new Laser(1, true), Direction.SOUTH)
        .build();
    new FieldBuilder("56") // Double laser stick on SOUTH and NORTH is a Wall
        .setNb(Direction.EAST, Direction.WEST)
        .setOp(new Laser(2, true), Direction.SOUTH)
        .build();
    new FieldBuilder("50") // Laser stick on EAST and WEST triple laser
        .setNb(Direction.EAST, Direction.WEST)
        .setOp(new Laser(3, true), Direction.SOUTH)
        .build();
    new FieldBuilder("51") // Laser stick on EAST and WEST wall
        .setNb(Direction.EAST, Direction.WEST)
        .setOp(new Laser(2, true), Direction.SOUTH)
        .build();
    new FieldBuilder("55") // Laser stick on EAST and WEST wall
        .setNb(Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Laser(1, false), Direction.SOUTH)
        .build();

    // Repair
    new FieldBuilder("66") // Repair field with one wrench, no walls
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Repair(1))
        .build();
    new FieldBuilder("67") // Repair field with one wrench, one wall
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new Repair(1))
        .build();
    new FieldBuilder("68") // Repair field with two wrenches, no walls
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Repair(2))
        .build();
    new FieldBuilder("69") // Repair field with two wrenches, two walls
        .setNb(Direction.NORTH, Direction.WEST)
        .setOp(new Repair(2))
        .build();

    // Gear
    new FieldBuilder("37") // counterclockwise (left) rotation gear
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Gear())
        .build();
    new FieldBuilder("38") // clockwise (right) rotation gear
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Gear())
        .build();

    // Belts
    new FieldBuilder("05") // Expressbelt
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .build();
    new FieldBuilder("14") // Belt
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("12") // Belt Curve Left
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.WEST)
        .build();
    new FieldBuilder("13") // Belt Curve Right
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.EAST)
        .build();
    new FieldBuilder("16") // Belt Interchange Right
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("15") // Belt Interchange Left
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("20") // Belt Interchange Right Left
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("21") // Belt Interchange Right Left
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("04") // Expressbelt Curve Right
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.EAST)
        .build();
    new FieldBuilder("03") // Expressbelt Curve Left
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.WEST)
        .build();
    new FieldBuilder("08") // Expressbelt Interchange Right Left
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .build();
    new FieldBuilder("07") // Expressbelt Interchange Right
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .build();
    new FieldBuilder("06") // Expressbelt Interchange Left
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .build();
    new FieldBuilder("09") // Expressbelt Startpoint
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .build();
    new FieldBuilder("10") // Expressbelt wall on EAST
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .build();
    new FieldBuilder("11") // Expressbelt wall in WEST
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .build();
    new FieldBuilder("17") // Belt wall on EAST
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("18") // Belt wall in WEST
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("19") // Belt Startpoint
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .build();
    new FieldBuilder("22") // Belt curve left with wall on top
        .setNb(Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.WEST)
        .build();
    new FieldBuilder("23") // Belt curve left with wall on EAST
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.WEST)
        .build();
    new FieldBuilder("24") // Belt curve right with wall on top
        .setNb(Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.EAST)
        .build();
    new FieldBuilder("25") // Belt curve right with wall on West
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.EAST)
        .setOp(new Belt(), Direction.EAST)
        .build();
    new FieldBuilder("26") // Belt curve left with wall on East and Top
        .setNb(Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.WEST)
        .build();
    // (Express)Belt with Lasers
    new FieldBuilder("80") // Expressbelt curve left wall East with Laser
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.WEST)
        .setOp(new Laser(1, false), Direction.WEST)
        .build();
    new FieldBuilder("81") // Expressbelt curve left wall West with Laser
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.EAST)
        .setOp(new ExpressBelt(), Direction.EAST)
        .setOp(new Laser(1, false), Direction.EAST)
        .build();
    new FieldBuilder("82") // Expressbelt  wall East with Laser
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .setOp(new Laser(1, false), Direction.EAST)
        .build();
    new FieldBuilder("83") // Expressbelt  wall West with Laser
        .setNb(Direction.NORTH, Direction.SOUTH, Direction.EAST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .setOp(new Laser(1, false), Direction.WEST)
        .build();
    new FieldBuilder("84") // Expressbelt with Laser
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .setOp(new Laser(1, false), Direction.WEST, Direction.EAST)
        .build();
    new FieldBuilder("85") // Expressbelt with Wall West and Laser
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .setOp(new Laser(1, true), Direction.WEST)
        .build();
    new FieldBuilder("86") // Expressbelt with Wall East and Laser
        .setNb(Direction.NORTH, Direction.WEST, Direction.SOUTH)
        .setOp(new ExpressBelt(), Direction.NORTH)
        .setOp(new Laser(1, true), Direction.EAST)
        .build();
    new FieldBuilder("87") // Belt curve Left with Wall East and Laser
        .setNb(Direction.NORTH, Direction.WEST, Direction.SOUTH)
        .setOp(new Belt(), Direction.WEST)
        .setOp(new Laser(1, true), Direction.EAST)
        .build();
    new FieldBuilder("88") // Belt curve Right with Wall West and Laser
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH)
        .setOp(new Belt(), Direction.EAST)
        .setOp(new Laser(1, true), Direction.WEST)
        .build();
    new FieldBuilder("89") // Belt with Wall East and Laser
        .setNb(Direction.NORTH, Direction.WEST, Direction.SOUTH)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Laser(1, true), Direction.WEST)
        .build();
    new FieldBuilder("90") // Belt with Wall West and Laser
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Laser(1, true), Direction.EAST)
        .build();
    new FieldBuilder("91") // Belt with Wall East and Laser
        .setNb(Direction.NORTH, Direction.WEST, Direction.SOUTH)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Laser(1, false), Direction.EAST)
        .build();
    new FieldBuilder("92") // Belt with Wall West and Laser
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Laser(1, false), Direction.WEST)
        .build();
    new FieldBuilder("93") // Belt with Wall West and Laser
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Laser(1, false), Direction.WEST, Direction.EAST)
        .build();

    // Expressbelt with Pusher
    new FieldBuilder("94") // Expressbelt with Wall South sticks a Pusher
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.EAST)
        .setOp(new Pusher(new Integer[] {1, 3, 5}), Direction.SOUTH)
        .build();
    new FieldBuilder("95") // Expressbelt with Wall South sticks a Pusher
        .setNb(Direction.NORTH, Direction.EAST, Direction.WEST)
        .setOp(new ExpressBelt(), Direction.EAST)
        .setOp(new Pusher(new Integer[] {2, 4}), Direction.SOUTH)
        .build();

    // Belt with Press
    new FieldBuilder("39") // Belt with Press
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Press(new Integer[] {1, 5}))
        .build();
    new FieldBuilder("40") // Belt with Press
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Press(new Integer[] {2, 4}))
        .build();
    new FieldBuilder("41") // Belt interchange right with Press
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Press(new Integer[] {2, 4}))
        .build();
    new FieldBuilder("42") // Belt interchange right with Press
        .setNb(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .setOp(new Belt(), Direction.NORTH)
        .setOp(new Press(3))
        .build();
  }

  private Library() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Getter for a specific basic floor field by its ID.
   *
   * @param id the ID of the basic floor field
   * @return the basic floor field
   */
  public static BasicFloorField get(String id) {
    try {
      return map.get(id);
    } catch (NullPointerException e) {
      return null;
    }
  }

  private static class FieldBuilder {

    private final Map<Integer, Operation> op = new HashMap<>();
    private final ArrayList<Direction> nb = new ArrayList<>();
    private final String id;

    private FieldBuilder(String id) {
      this.id = id;
    }

    private void build() {
      if (nb.isEmpty() || nb.size() > 4) {
        throw new IllegalStateException("Field has forbidden number of neighbours");
      }
      map.put(id, new BasicFloorField(id, nb, op));
    }

    private FieldBuilder setOp(Operation op, Direction... d) {
      ArrayList<Direction> l = new ArrayList<>(Arrays.asList(d));
      op.setDirections(l);
      List<Class<? extends Operation>> classes =
          new ArrayList<>(
              Arrays.asList(
                  ExpressBelt.class,
                  Belt.class,
                  Pusher.class,
                  Gear.class,
                  Press.class,
                  Laser.class,
                  Repair.class,
                  Checkpoint.class));
      this.op.put(classes.indexOf(op.getClass()), op);
      return this;
    }

    private FieldBuilder setNb(Direction... nb) {
      this.nb.addAll(Arrays.asList(nb));
      return this;
    }
  }
}
