package de.uol.swp.common.game.animation;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robots;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** ParallelAnimation allows multiple SingleAnimations at the same time. */
public class ParallelAnimation implements Animation, Serializable {

  private static final long serialVersionUID = 2408507349172944924L;
  private final List<Animation> animations = new ArrayList<>();

  public void addAnimation(Animation animation) {
    animations.add(animation);
  }

  public List<Animation> getAnimations() {
    return animations;
  }

  @Override
  public AnimationType getType() {
    return null;
  }

  @Override
  public Robots getRobot() {
    return null;
  }

  @Override
  public Direction getDirection() {
    return null;
  }

  @Override
  public Boolean isParallel() {
    return true;
  }

  @Override
  public String getName() {
    return "ParallelAnimation";
  }
}
