package de.uol.swp.common.game.animation;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robots;
import java.io.Serializable;

/** The SingleAnimation saves the animation type and the corresponding robot. */
public class SingleAnimation implements Animation, Serializable {

  private static final long serialVersionUID = -7504993293011939103L;
  private final AnimationType type;
  private final Robots robot;
  private final int fromX;
  private final int fromY;
  private final Direction wasFacing;
  private int count;

  /**
   * Constructor.
   *
   * @param type AnimationType
   * @param robot RobotType
   * @param fromX From X
   * @param fromY From Y
   * @param wasFacing wasFacing
   */
  public SingleAnimation(
      AnimationType type, Robots robot, int fromX, int fromY, Direction wasFacing) {
    this.type = type;
    this.robot = robot;
    this.fromX = fromX;
    this.fromY = fromY;
    this.wasFacing = wasFacing;
  }

  /**
   * Constructor.
   *
   * @param type AnimationType
   * @param robot RobotType
   * @param fromX From X
   * @param fromY From Y
   * @param wasFacing wasFacing
   */
  public SingleAnimation(
      AnimationType type, Robots robot, int fromX, int fromY, Direction wasFacing, int count) {
    this.type = type;
    this.robot = robot;
    this.fromX = fromX;
    this.fromY = fromY;
    this.wasFacing = wasFacing;
    this.count = count;
  }

  public AnimationType getType() {
    return type;
  }

  public Robots getRobot() {
    return robot;
  }

  @Override
  public Direction getDirection() {
    return wasFacing;
  }

  @Override
  public Boolean isParallel() {
    return false;
  }

  @Override
  public String getName() {
    return "SingleAnimation";
  }

  public int getFromX() {
    return fromX;
  }

  public int getFromY() {
    return fromY;
  }

  public int getCount() {
    return count;
  }
}
