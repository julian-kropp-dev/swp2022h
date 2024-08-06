package de.uol.swp.common.game.floor;

import java.io.Serializable;

/** Class for floor field which are the base of the floor plan. */
public class FloorField implements Serializable {

  private static final long serialVersionUID = 4595280060125099200L;
  private final Direction direction;
  private final BasicFloorField basicFloorField;

  @SuppressWarnings("checkstyle:MemberName")
  private final int x;

  @SuppressWarnings("checkstyle:MemberName")
  private final int y;

  private FloorField north;
  private FloorField east;
  private FloorField south;
  private FloorField west;

  /**
   * Constructor to create a new floor field by the underlying basic floor field and a direction.
   *
   * @param direction the direction which rotates the basic floor field
   * @param basicFloorField the underlying basic floor field
   */
  public FloorField(Direction direction, BasicFloorField basicFloorField, int x, int y) {
    this.direction = direction;
    this.basicFloorField = basicFloorField;
    this.x = x;
    this.y = y;
  }

  /**
   * Getter for all Neighbours.
   *
   * @param dir the direction of the requested neighbour
   * @return the neighbour
   */
  public FloorField getNeighbour(Direction dir) {
    switch (dir) {
      case NORTH:
        return north;
      case EAST:
        return east;
      case SOUTH:
        return south;
      case WEST:
        return west;
      default:
        throw new IllegalArgumentException("Direction does not exist");
    }
  }

  /** Setter for the north neighbour. */
  public void setNorth(FloorField north) {
    this.north = north;
  }

  /** Setter for the north neighbour. */
  public void setEast(FloorField east) {
    this.east = east;
  }

  /** Setter for the north neighbour. */
  public void setSouth(FloorField south) {
    this.south = south;
  }

  /** Setter for the north neighbour. */
  public void setWest(FloorField west) {
    this.west = west;
  }

  /**
   * Getter for the basic floor field.
   *
   * @return the basic floor field
   */
  public BasicFloorField getBasicFloorField() {
    return basicFloorField;
  }

  /**
   * Getter for the direction.
   *
   * @return the direction
   */
  public Direction getDirection() {
    return direction;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
