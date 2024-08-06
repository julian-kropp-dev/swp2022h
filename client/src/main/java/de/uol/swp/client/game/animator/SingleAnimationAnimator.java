package de.uol.swp.client.game.animator;

import de.uol.swp.client.game.GamePresenter;
import de.uol.swp.common.game.animation.AnimationType;
import de.uol.swp.common.game.animation.SingleAnimation;
import de.uol.swp.common.game.floor.Direction;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/** Class for generating keyframes for a SingleAnimation. */
public class SingleAnimationAnimator {

  private final GamePresenter gamePresenter;
  Duration duration;

  public SingleAnimationAnimator(GamePresenter gamePresenter, Duration duration) {
    this.gamePresenter = gamePresenter;
    this.duration = duration;
  }

  /**
   * Generates keyframes for a SingleAnimation.
   *
   * @param animation the SingleAnimation
   * @param timestamp the timestamp when the SingleAnimation should start playing
   * @return the Keyframe
   */
  public KeyFrame getKeyFrame(SingleAnimation animation, int timestamp) {
    AnimationType typeOfAnimation = animation.getType();
    AnimationInformation info;
    switch (typeOfAnimation) {
      case WALL_NORTH:
        info = new AnimationInformation(Direction.NORTH, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.wall(info));
      case WALL_EAST:
        info = new AnimationInformation(Direction.EAST, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.wall(info));
      case WALL_SOUTH:
        info = new AnimationInformation(Direction.SOUTH, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.wall(info));
      case WALL_WEST:
        info = new AnimationInformation(Direction.WEST, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.wall(info));
      case MOVED_NORTH:
      case MOVED_NORTH_BY_BELT:
        info = new AnimationInformation(Direction.NORTH, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.move(info));
      case MOVED_EAST:
      case MOVED_EAST_BY_BELT:
        info = new AnimationInformation(Direction.EAST, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.move(info));
      case MOVED_SOUTH:
      case MOVED_SOUTH_BY_BELT:
        info = new AnimationInformation(Direction.SOUTH, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.move(info));
      case MOVED_WEST:
      case MOVED_WEST_BY_BELT:
        info = new AnimationInformation(Direction.WEST, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.move(info));
      case TURN_LEFT:
        info = new AnimationInformation(Direction.NORTH, animation.getRobot(), -90);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.rotation(info));
      case TURN_RIGHT:
        info = new AnimationInformation(Direction.NORTH, animation.getRobot(), 90);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.rotation(info));
      case U_TURN:
        info = new AnimationInformation(Direction.NORTH, animation.getRobot(), 180);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.rotation(info));
      case NO_TURN:
        info = new AnimationInformation(Direction.NORTH, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.rotation(info));
      case PIT:
        info = new AnimationInformation(animation.getDirection(), animation.getRobot());
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.fallIntoPit(info));
      case GAME_OVER: // unused
        break;
      case WIN:
        break; // unused
      case ARCHIVE:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event ->
                gamePresenter.updateArchiveMarker(
                    animation.getRobot(), animation.getFromX(), animation.getFromY()));
      case CHECKPOINT:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateCheckpoint(animation.getCount(), animation.getRobot());
              gamePresenter.updateArchiveMarker(
                  animation.getRobot(), animation.getFromX(), animation.getFromY());
            });
      case REPAIR:
        info = new AnimationInformation(null, animation.getRobot(), 0);
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.repairRobot(info));
      case LASER_DAMAGE:
        info = new AnimationInformation(animation.getRobot());
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.laserDamage(info));
      case DESTROYED: // unused
        break;
      case PRESS:
        info = new AnimationInformation(animation.getRobot());
        return new KeyFrame(Duration.seconds(timestamp), event -> gamePresenter.press(info));
      default:
        // default
        break;
    }
    return null;
  }
}
