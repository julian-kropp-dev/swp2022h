package de.uol.swp.common.game.animation;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robots;
import java.io.Serializable;

/** The interface for all classes of animations. */
public interface Animation extends Serializable {
  AnimationType getType();

  Robots getRobot();

  Direction getDirection();

  /**
   * Getter if the animation is parallel.
   *
   * @return true, if the animations are parallel
   */
  Boolean isParallel();

  String getName();
}
