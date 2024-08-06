package de.uol.swp.common.game.animation;

import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robots;
import java.io.Serializable;

/** Pause between Animations. */
public class SeparationPoint implements Animation, Serializable {

  private static final long serialVersionUID = -6663691965694872051L;
  private final AnimationType animationType;
  private CardType cardType;
  private int cardCount;
  private Robots robot;
  private int lives = 3;
  private int damage = 0;

  public SeparationPoint(AnimationType animationType) {
    this.animationType = animationType;
  }

  /**
   * Constructs a SeparationPoint animation with the specified parameters.
   *
   * @param animationType the type of the animation
   * @param robot the robot associated with the animation
   * @param lives the number of lives
   * @param damage the amount of damage
   */
  public SeparationPoint(AnimationType animationType, Robots robot, int lives, int damage) {
    this.animationType = animationType;
    this.robot = robot;
    this.lives = lives;
    this.damage = damage;
  }

  /**
   * Constructs a SeparationPoint animation with the specified parameters.
   *
   * @param animationType the type of the animation
   * @param cardType the type of the card
   * @param cardCount the number of cards
   * @param robot the robot associated with the animation
   */
  public SeparationPoint(
      AnimationType animationType, CardType cardType, int cardCount, Robots robot) {
    this.animationType = animationType;
    this.cardType = cardType;
    this.cardCount = cardCount;
    this.robot = robot;
  }

  @Override
  public AnimationType getType() {
    return animationType;
  }

  @Override
  public Robots getRobot() {
    return robot;
  }

  @Override
  public Direction getDirection() {
    return null;
  }

  public CardType getCardType() {
    return cardType;
  }

  @SuppressWarnings("unused")
  public int getCardCount() {
    return cardCount;
  }

  @Override
  public Boolean isParallel() {
    return false;
  }

  @Override
  public String getName() {
    return "SeparationPoint";
  }

  public int getLives() {
    return lives;
  }

  public int getDamage() {
    return damage;
  }
}
