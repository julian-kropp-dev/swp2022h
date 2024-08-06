package de.uol.swp.common.game.card;

import de.uol.swp.common.game.floor.Direction;

/** Enum for card types with direction and steps. */
public enum CardType {
  MOVE1(1, Direction.NORTH),
  MOVE2(2, Direction.NORTH),
  MOVE3(3, Direction.NORTH),
  BACKUP(-1, Direction.NORTH),
  TURN_LEFT(0, Direction.WEST),
  TURN_RIGHT(0, Direction.EAST),
  U_TURN(0, Direction.SOUTH);

  private final int steps;
  private final Direction direction;

  CardType(int steps, Direction direction) {
    this.steps = steps;
    this.direction = direction;
  }

  /**
   * This method give a true back if the showRobotInfo Filed needs a number for a card-type.
   *
   * @param cardType The card-type
   * @return true if a number is needed
   */
  @SuppressWarnings({"SummaryJavadoc", "MissingSwitchDefault"})
  public static boolean numberForCardNeeded(CardType cardType) {
    switch (cardType) {
      case MOVE1:
      case MOVE2:
      case MOVE3:
        return true;
      case BACKUP:
      case TURN_LEFT:
      case TURN_RIGHT:
      case U_TURN:
        return false;
    }
    return false;
  }

  /**
   * Getter for the steps of this card.
   *
   * @return amount of steps
   */
  public int getSteps() {
    return steps;
  }

  /**
   * Getter for direction of this card.
   *
   * @return the direction
   */
  public Direction getDirection() {
    return direction;
  }
}
