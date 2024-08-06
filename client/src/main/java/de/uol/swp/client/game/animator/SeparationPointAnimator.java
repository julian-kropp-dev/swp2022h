package de.uol.swp.client.game.animator;

import de.uol.swp.client.game.GamePresenter;
import de.uol.swp.common.game.animation.AnimationType;
import de.uol.swp.common.game.animation.SeparationPoint;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class for generating keyframes for a SeparationPoint. */
public class SeparationPointAnimator {

  private static final Logger LOG = LogManager.getLogger(SeparationPointAnimator.class);

  private static final String PRESS = "Fabrikphase Pressen";
  private static final String PUSHER = "Fabrikphase Schieber";
  private final GamePresenter gamePresenter;
  private final FloorFieldAnimator floorFieldAnimator;
  Duration duration;

  /**
   * Constructor.
   *
   * @param gamePresenter GamePresenter
   * @param duration Duration
   */
  public SeparationPointAnimator(GamePresenter gamePresenter, Duration duration) {
    this.gamePresenter = gamePresenter;
    this.duration = duration;
    this.floorFieldAnimator = gamePresenter.getFloorFieldAnimator();
  }

  /**
   * Generates keyframes for a SeparationPoint.
   *
   * @param animation the SeparationPoint
   * @param timestamp the timestamp when the SingleAnimation should start playing
   * @return the keyframe
   */
  public KeyFrame getKeyFrame(SeparationPoint animation, int timestamp) {
    AnimationType typeOfAnimation = animation.getType();
    switch (typeOfAnimation) {
      case MOVEMENT:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> gamePresenter.updateGamePhaseText("Programmablauf startet"));
      case FACTORY_PHASE_START:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase erste Runde");
              gamePresenter.clearDisplayNextCard();
            });
      case FACTORY_PHASE_ROUND_TWO:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase zweite Runde");
              gamePresenter.clearDisplayNextCard();
            });
      case FACTORY_PHASE_ROUND_THREE:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase dritte Runde");
              gamePresenter.clearDisplayNextCard();
            });
      case FACTORY_PHASE_ROUND_FOUR:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase vierte Runde");
              gamePresenter.clearDisplayNextCard();
            });
      case FACTORY_PHASE_ROUND_FIVE:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase fünfte Runde");
              gamePresenter.clearDisplayNextCard();
            });
      case FACTORY_PHASE_EXPRESS_ONE:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase Expressföderbänder");
              floorFieldAnimator.getExpressBeltParallelTransition().play();
            });
      case FACTORY_PHASE_EXPRESS_TWO_AND_NORMAL:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase Föderbänder");
              floorFieldAnimator.getBeltParallelTransition().play();
            });
      case FACTORY_PHASE_PUSHER1:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PUSHER);
              floorFieldAnimator.getPusher1ParallelTransition().play();
            });
      case FACTORY_PHASE_PUSHER2:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PUSHER);
              floorFieldAnimator.getPusher2ParallelTransition().play();
            });
      case FACTORY_PHASE_PUSHER3:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PUSHER);
              floorFieldAnimator.getPusher3ParallelTransition().play();
            });
      case FACTORY_PHASE_PUSHER4:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PUSHER);
              floorFieldAnimator.getPusher4ParallelTransition().play();
            });
      case FACTORY_PHASE_PUSHER5:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PUSHER);
              floorFieldAnimator.getPusher5ParallelTransition().play();
            });
      case FACTORY_PHASE_GEARS:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText("Fabrikphase Zahnräder");
              floorFieldAnimator.getGearParallelTransition().play();
            });
      case FACTORY_PHASE_PRESS1:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PRESS);
              floorFieldAnimator.getPress1ParallelTransition().play();
            });
      case FACTORY_PHASE_PRESS2:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PRESS);
              floorFieldAnimator.getPress2ParallelTransition().play();
            });
      case FACTORY_PHASE_PRESS3:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PRESS);
              floorFieldAnimator.getPress3ParallelTransition().play();
            });
      case FACTORY_PHASE_PRESS4:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PRESS);
              floorFieldAnimator.getPress4ParallelTransition().play();
            });
      case FACTORY_PHASE_PRESS5:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PRESS);
              floorFieldAnimator.getPress5ParallelTransition().play();
            });
      case FACTORY_PHASE_LASER:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateGamePhaseText(PRESS);
              floorFieldAnimator.getLaserParallelTransition().play();
            });
      case FACTORY_PHASE_ROBOT_LASER:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> gamePresenter.updateGamePhaseText("Roboter Laser"));
      case DISPLAY_NEXT_CARD:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> gamePresenter.displayNextCard(animation.getCardType(), animation.getRobot()));
      case REPAIR_FIELD:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> floorFieldAnimator.getRepairParallelTransition().play());
      case UPDATE_DAMAGE_AND_LIVES:
        return new KeyFrame(
            Duration.seconds(timestamp),
            event -> {
              gamePresenter.updateDamagePoints(animation.getDamage(), animation.getRobot());
              gamePresenter.updatePlayerLives(animation.getLives(), animation.getRobot());
            });
      default:
        LOG.warn("Unknown AnimationType occurred");
        break;
    }
    return null;
  }
}
