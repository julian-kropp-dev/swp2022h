package de.uol.swp.common.game.card;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robots;
import java.io.Serializable;
import java.util.Objects;

/** Class for ProgrammingCards. */
public class ProgrammingCard implements Comparable<ProgrammingCard>, Serializable {
  private static final long serialVersionUID = -8229854893848036755L;
  private final int count;
  private final CardType cardType;
  private Robots owner;

  /**
   * Constructs a new ProgrammingCard instance with the specified count and card type.
   *
   * @param count the count of the card. Higher numbers indicate higher priority.
   * @param cardType the type of the card
   * @throws IllegalArgumentException if the count is negative.
   */
  public ProgrammingCard(int count, CardType cardType) {
    if (count < 0) {
      throw new IllegalArgumentException("Count cannot be negative.");
    } else if (cardType == null) {
      throw new IllegalArgumentException("CardType cannot be null.");
    }
    this.count = count;
    this.cardType = cardType;
  }

  /**
   * Returns the current Robot in which this card in inserted.
   *
   * @return Robot that is programmed with this card.
   */
  public Robots getOwner() {
    return owner;
  }

  /**
   * Setter for the owner.
   *
   * @param owner the Robot.
   */
  public void setOwner(Robots owner) {
    this.owner = owner;
  }

  /**
   * Returns the Count of the card. Higher numbers equals to a higher priority.
   *
   * @return count as an Integer.
   */
  public int getCount() {
    return count;
  }

  /**
   * Returns the CardType (eg. MOVE, UTURN, etc)
   *
   * @return CardType.
   * @see de.uol.swp.common.game.card.CardType
   */
  public CardType getCardType() {
    return cardType;
  }

  /**
   * Returns the angle as a direction in which the robot is turned when the card gets executed. The
   * direction assumes the default case that the robot faces north. When executing a card the
   * direction has to be translated.
   *
   * @return the Direction
   * @see de.uol.swp.common.game.floor.Direction
   */
  public Direction getDirection() {
    return this.cardType.getDirection();
  }

  /**
   * Returns the number of steps that the robot moves when a card is executed.
   *
   * @return Number of Steps.
   */
  public int getSteps() {
    return this.cardType.getSteps();
  }

  /**
   * Returns true if the Card has a type of MOVE1,MOVE2,MOVE3 or BACKUP.
   *
   * @return whether the Card is a Move
   */
  public boolean isMoveCard() {
    return (cardType == CardType.MOVE1
        || cardType == CardType.MOVE2
        || cardType == CardType.MOVE3
        || cardType == CardType.BACKUP);
  }

  @Override
  public int compareTo(ProgrammingCard o) {
    return this.count - o.count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProgrammingCard that = (ProgrammingCard) o;
    return count == that.count;
  }

  @Override
  public int hashCode() {
    return Objects.hash(count);
  }
}
