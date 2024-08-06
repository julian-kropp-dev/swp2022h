package de.uol.swp.client.game.animator;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robots;

/** Helper Class to store information of an animation. */
public class AnimationInformation {

  private final Direction direction;
  private final Robots robotType;
  private final int degrees;

  /**
   * Constructor.
   *
   * @param direction direction
   * @param robotType robot
   * @param degrees degrees
   */
  public AnimationInformation(Direction direction, Robots robotType, int degrees) {
    this.direction = direction;
    this.robotType = robotType;
    this.degrees = degrees;
  }

  /**
   * Constructor.
   *
   * @param direction direction
   * @param robot robot
   */
  public AnimationInformation(Direction direction, Robots robot) {
    this.robotType = robot;
    this.direction = direction;
    this.degrees = 0;
  }

  /**
   * Constructor.
   *
   * @param robotType robot
   */
  public AnimationInformation(Robots robotType) {
    this.robotType = robotType;
    this.direction = null;
    this.degrees = 0;
  }

  /**
   * Returns a translation factor for the x-axis.
   *
   * @return -1, 0 or 1
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public int getTranslationXFactor() {
    if (direction != null) {
      switch (direction) {
        case WEST:
          return -1;
        case EAST:
          return 1;
        default:
          return 0;
      }
    }
    return 0;
  }

  /**
   * Returns a translation factor for the y-axis.
   *
   * @return -1, 0 or 1
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public int getTranslationYFactor() {
    if (direction != null) {
      switch (direction) {
        case NORTH:
          return -1;
        case SOUTH:
          return 1;
        default:
          return 0;
      }
    }
    return 0;
  }

  /**
   * Getter.
   *
   * @return direction
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Getter.
   *
   * @return robot
   */
  public Robots getRobotType() {
    return robotType;
  }

  /**
   * Getter.
   *
   * @return degrees
   */
  public int getDegrees() {
    return degrees;
  }
}
