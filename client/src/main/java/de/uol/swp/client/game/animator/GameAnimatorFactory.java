package de.uol.swp.client.game.animator;

import de.uol.swp.client.game.GamePresenter;
import de.uol.swp.common.game.animation.Animation;
import de.uol.swp.common.game.animation.ParallelAnimation;
import de.uol.swp.common.game.animation.SeparationPoint;
import de.uol.swp.common.game.animation.SingleAnimation;
import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.game.dto.Step.RobotInformation;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robots;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The GameAnimatorFactory class is responsible for creating and managing game animations. */
public class GameAnimatorFactory {

  private static final Logger LOG = LogManager.getLogger(GameAnimatorFactory.class);
  private final GamePresenter gamePresenter;
  private final SingleAnimationAnimator singleAnimationAnimator;
  private final SeparationPointAnimator separationPointAnimator;

  /**
   * Constructor.
   *
   * @param gamePresenter gamePresenter
   */
  public GameAnimatorFactory(GamePresenter gamePresenter) {
    this.gamePresenter = gamePresenter;
    this.singleAnimationAnimator = new SingleAnimationAnimator(gamePresenter, Duration.seconds(1));
    this.separationPointAnimator = new SeparationPointAnimator(gamePresenter, Duration.seconds(1));
  }

  /**
   * Plays the animation for a step object.
   *
   * @param step Step-Object
   */
  public void playAnimation(Step step) {
    int stepCount = step.getSteps().size();
    Timeline timeline = new Timeline();
    for (int i = 0; i < stepCount; i++) {
      Animation animation = step.getSteps().get(i);
      if (isAnimationParallel(animation)) {
        List<KeyFrame> frames = generateFramesParallelAnimation(animation, (i * 2) + 1);
        timeline.getKeyFrames().addAll(frames);
      } else {
        KeyFrame keyFrame = generateFrameForSingeAnimation(animation, (i * 2) + 1);
        if (keyFrame != null) {
          timeline.getKeyFrames().add(keyFrame);
        }
      }
    }
    timeline.play();
    if (stepCount > 1) { // stepCount == 1 -> moveRobotCommand, sync-methods unnecessary
      timeline.setOnFinished(
          event -> {
            syncAllRobotPositions(step.getLatestRobotInformation());
            syncGui(step.getLatestRobotInformation());
          });
    }
  }

  /**
   * This method updates the gui for one robot.
   *
   * @param robot robot
   * @param robotInformation robotInformation
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void updateGui(Robots robot, RobotInformation robotInformation) {
    Pane robotField = gamePresenter.getRobotField();
    int tileSize = gamePresenter.getTileSize();
    Direction robotDirection = robotInformation.getCurrentDirection();
    int newXRobot = robotInformation.getCurrentField().getX();
    int newYRobot = robotInformation.getCurrentField().getY();
    for (Node node : robotField.getChildren()) {
      if (node instanceof ImageView) {
        ImageView imageView = (ImageView) node;
        String id = imageView.getId();
        if (id != null && id.equals("robot-" + robot.ordinal())) {
          if (robotInformation.getHealth() == 0) {
            Platform.runLater(() -> imageView.setImage(null));
          } else {
            LOG.trace("Update Robot {} to X {} - Y {}", robot, newXRobot, newYRobot);
            Platform.runLater(
                () -> {
                  imageView.setTranslateX(newXRobot * tileSize * 1.0D);
                  imageView.setTranslateY(newYRobot * tileSize * 1.0D);
                  imageView.setRotate((robotDirection.ordinal() - 1) * 90.0D);
                  imageView.setScaleX(1.0);
                  imageView.setScaleY(1.0);
                });
          }
        }
      }
    }
    gamePresenter.updateCheckpoint(robotInformation.getLatestCheckpoint(), robot);
    gamePresenter.updatePlayerLives(robotInformation.getHealth(), robot);
    gamePresenter.updateDamagePoints(robotInformation.getReceivedDamage(), robot);
  }

  private boolean isAnimationParallel(Animation animation) {
    return animation.isParallel() && animation.getName().equals("ParallelAnimation");
  }

  private KeyFrame generateFrameForSingeAnimation(Animation animation, int timestamp) {
    switch (animation.getName()) {
      case "SeparationPoint":
        SeparationPoint separationPoint = (SeparationPoint) animation;
        return separationPointAnimator.getKeyFrame(separationPoint, timestamp);
      case "SingleAnimation":
        SingleAnimation singleAnimation = (SingleAnimation) animation;
        return singleAnimationAnimator.getKeyFrame(singleAnimation, timestamp);
      default:
        return null;
    }
  }

  private List<KeyFrame> generateFramesParallelAnimation(Animation animation, int timestamp) {
    ParallelAnimation parallelAnimation = (ParallelAnimation) animation;
    List<Animation> subStepAnimations = parallelAnimation.getAnimations();
    List<KeyFrame> keyFrames = new ArrayList<>();
    for (Animation subStepAnimation : subStepAnimations) {
      KeyFrame keyFrame = generateFrameForSingeAnimation(subStepAnimation, timestamp);
      if (keyFrame != null) {
        keyFrames.add(keyFrame);
      }
    }
    return keyFrames;
  }

  /**
   * Updates the robot and archive marker position.
   *
   * @param robotInformationMap Information
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void syncAllRobotPositions(Map<Robots, RobotInformation> robotInformationMap) {
    Pane robotField = gamePresenter.getRobotField();
    robotInformationMap.forEach(
        (robot, robotInformation) -> {
          if (robotInformation.getCurrentField() != null) {
            int newXRobot = robotInformation.getCurrentField().getX();
            int newYRobot = robotInformation.getCurrentField().getY();
            int newXMarker = robotInformation.getRespawn().getX();
            int newYMarker = robotInformation.getRespawn().getY();
            int tileSize = gamePresenter.getTileSize();
            Direction robotDirection = robotInformation.getCurrentDirection();

            updateRobot(robotField, tileSize, newXRobot, newYRobot, robot, robotDirection);
            updateMarker(robotField, tileSize, newXMarker, newYMarker, robot);
          }
        });
  }

  /** Updates the position of the marker. */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void updateMarker(
      Pane robotField, int tileSize, int newXMarker, int newYMarker, Robots robot) {
    for (Node node : robotField.getChildren()) {
      if (node instanceof ImageView) {
        ImageView imageView = (ImageView) node;
        String id = imageView.getId();
        if (id != null && id.equals("marker-" + robot.ordinal())) {
          imageView.setTranslateX(newXMarker * (double) tileSize);
          imageView.setTranslateY(newYMarker * (double) tileSize);
        }
      }
    }
  }

  /** Updates the position and direction of the robot. */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void updateRobot(
      Pane robotField,
      int tileSize,
      int newXRobot,
      int newYRobot,
      Robots robot,
      Direction robotDirection) {
    for (Node node : robotField.getChildren()) {
      if (node instanceof ImageView) {
        ImageView imageView = (ImageView) node;
        String id = imageView.getId();
        if (id != null && id.equals("robot-" + robot.ordinal())) {
          imageView.setTranslateY(newXRobot * (double) tileSize);
          imageView.setTranslateY(newYRobot * (double) tileSize);
          imageView.setRotate((robotDirection.ordinal() - 1) * (double) 90);
        }
      }
    }
  }

  /**
   * Updates the checkpoint, lives and damage information for all robots.
   *
   * @param robotInformationMap Information
   */
  public void syncGui(Map<Robots, RobotInformation> robotInformationMap) {
    robotInformationMap.forEach(
        (robot, robotInformation) -> {
          LOG.trace(
              "CP: {}, H: {}, Dam: {} for Robot {}",
              robotInformation.getLatestCheckpoint(),
              robotInformation.getHealth(),
              robotInformation.getReceivedDamage(),
              robot);
          gamePresenter.updateCheckpoint(robotInformation.getLatestCheckpoint(), robot);
          gamePresenter.updatePlayerLives(robotInformation.getHealth(), robot);
          gamePresenter.updateDamagePoints(robotInformation.getReceivedDamage(), robot);
        });
  }
}
