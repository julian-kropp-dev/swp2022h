package de.uol.swp.common.game.floor;

/** Enumeration for the four directions of a field. */
public enum Direction {
  NORTH(0),
  EAST(1),
  SOUTH(2),
  WEST(3);

  final int number;

  /**
   * Constructor to map a number to the direction.
   *
   * @param i number of direction
   */
  Direction(int i) {
    this.number = i;
  }

  /**
   * Get a direction by its number.
   *
   * @param i the number of the direction
   * @return the direction with this number
   */
  public static Direction get(int i) {
    for (Direction dir : Direction.values()) {
      if (i == dir.number) {
        return dir;
      }
    }
    return null;
  }

  /**
   * Get number by its direction.
   *
   * @return the number of this direction
   */
  public int getNumber() {
    return number;
  }

  /**
   * Rotate a direction by another direction.
   *
   * @param direction the direction by which it should be rotated
   * @return the new direction
   */
  public Direction rotate(Direction direction) {
    for (Direction dir : Direction.values()) {
      if ((number + direction.number) % 4 == dir.number) {
        return dir;
      }
    }
    throw new IllegalStateException("Direction does not exist");
  }
}
