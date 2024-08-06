package de.uol.swp.common.game.robot;

import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.dto.Step.RobotInformation;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Class for the robot on the floor plan. */
public class Robot implements Serializable {

  public static final int MEMORY_SIZE = 9;
  public static final int PROGRAM_SIZE = 5;
  private static final long serialVersionUID = 2018680859649576452L;
  private final Robots type;
  private final ProgrammingCard[] handCards = new ProgrammingCard[MEMORY_SIZE];
  private final ProgrammingCard[] selectedCards = new ProgrammingCard[PROGRAM_SIZE];
  private FloorField position;
  private FloorField respawn;
  private Direction direction = Direction.EAST;
  private boolean alive = true;
  private int lives = 3;
  private int damage = 0;
  private boolean[] blocked = new boolean[] {false, false, false, false, false};
  private int currentCheckpoint = 0;
  private boolean lastMoveByBelt;
  private boolean needGuiInteraction;
  private boolean isShutdown;
  private boolean isNeedToShutdown;

  /**
   * Constructor with the robot type and its start position.
   *
   * @param type type of the robot
   * @param start start position
   */
  public Robot(Robots type, FloorField start) {
    if (type == Robots.DUMMY) {
      this.alive = false;
      this.lives = 0;
    }
    this.type = type;
    this.position = start;
    this.respawn = start;
    resetDamage();
  }

  // -------------------------------------------------------------------------------
  // Getter and Setter
  // -------------------------------------------------------------------------------

  /**
   * Method to unblock Cards.
   *
   * @param toUnblock slot to unblock
   */
  public void unblockSlot(int toUnblock) {
    if (isBlocked(toUnblock)) {
      int count = 0;
      for (boolean b : blocked) {
        if (b) {
          count++;
        }
      }
      if (count > damage - 4) {
        blocked[toUnblock] = false;
      }
    }
  }

  /**
   * Setter for new selected cards. Cards are only settable when not blocked
   *
   * @param cards the new cards
   */
  public void setSelectedCardsTesting(ProgrammingCard[] cards) {
    for (int i = 0; i < cards.length; i++) {
      if (!blocked[i]) {
        this.selectedCards[i] = cards[i];
        this.selectedCards[i].setOwner(type);
      }
    }
  }

  /**
   * Getter for the type of the robot.
   *
   * @return the type
   */
  public Robots getType() {
    return type;
  }

  /**
   * Getter for the position of the robot.
   *
   * @return the position
   */
  public FloorField getPosition() {
    return position;
  }

  /**
   * Setter for the new position of the robot.
   *
   * @param position the new position
   */
  public void setPosition(FloorField position) {
    this.position = position;
  }

  /**
   * Setter for the respawn field.
   *
   * @param respawn the new respawn field
   */
  public void setRespawn(FloorField respawn) {
    this.respawn = respawn;
  }

  /**
   * Getter for the direction of the robot.
   *
   * @return the direction
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Setter for the direction of the robot.
   *
   * @param direction the new direction
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * Getter for the alive boolean.
   *
   * @return if the roboter died this round
   */
  public boolean isAlive() {
    return alive;
  }

  /** Sets the alive boolean to true. */
  public void alive() {
    this.alive = true;
  }

  /**
   * Getter for the number of remaining lives.
   *
   * @return number of remaining lives
   */
  public int getLives() {
    return lives;
  }

  /**
   * Getter for the present damage to the robot.
   *
   * @return the present damage
   */
  public int getDamage() {
    return damage;
  }

  /** Resets the damage to the robot to zero. */
  public void resetDamage() {
    damage = 0;
    blocked = new boolean[] {false, false, false, false, false};
  }

  /**
   * Getter for the programming cards in hand.
   *
   * @return the cards of this robot
   */
  public ProgrammingCard[] getHandCards() {
    return handCards;
  }

  /**
   * Setter for new cards. The amount is based on the damage.
   *
   * @param cards the new cards
   */
  public void setHandCards(ProgrammingCard[] cards) {
    for (int i = 0; i < MEMORY_SIZE; i++) {
      if (i < MEMORY_SIZE - damage && i < cards.length) {
        this.handCards[i] = cards[i];
      } else {
        this.handCards[i] = null;
      }
    }
  }

  /**
   * Getter for the programming cards selected.
   *
   * @return the cards of this robot
   */
  public ProgrammingCard[] getSelectedCards() {
    return selectedCards;
  }

  /**
   * Setter for new selected cards. Cards are only settable when not blocked
   *
   * @param cards the new cards
   */
  public void setSelectedCards(ProgrammingCard[] cards) {
    for (int i = 0; i < cards.length; i++) {
      if (!blocked[i]) {
        this.selectedCards[i] = cards[i];
      }
    }
  }

  /**
   * The method returns the blocked program slots.
   *
   * @return a boolean array of blocked program slots
   */
  public boolean[] getBlocked() {
    return blocked;
  }

  /**
   * Setter for blocked slots.
   *
   * @param blocked array of blocked slots
   */
  public void setBlocked(boolean[] blocked) {
    if (blocked.length == PROGRAM_SIZE) {
      this.blocked = blocked;
    }
  }

  /**
   * The method returns whether a specific slot of the robot is blocked.
   *
   * @param i The index of the slot
   * @return true if blocked
   */
  public boolean isBlocked(int i) {
    return blocked[i];
  }

  /**
   * Getter for the last checkpoint reached.
   *
   * @return the currentCheckpoint of this robot
   */
  public int getCurrentCheckpoint() {
    return currentCheckpoint;
  }

  /**
   * Setter for last checkpoint reached.
   *
   * @param currentCheckpoint the checkpoint that was just reached
   */
  public void setCurrentCheckpoint(int currentCheckpoint) {
    this.currentCheckpoint = currentCheckpoint;
  }

  /**
   * Getter for LastMoveByBelt.
   *
   * @return boolean if robot was last moved by a Belt
   */
  public boolean isLastMoveByBelt() {
    return lastMoveByBelt;
  }

  /**
   * Setter for LastMoveByBelt.
   *
   * @param lastMoveByBelt boolean if robot was last moved by a Belt
   */
  public void setLastMoveByBelt(boolean lastMoveByBelt) {
    this.lastMoveByBelt = lastMoveByBelt;
  }

  /**
   * Getter for needGuiInteraction.
   *
   * @return boolean if Gui Interaction is needed
   */
  public boolean isNeedGuiInteraction() {
    return needGuiInteraction;
  }

  /**
   * Setter for needGuiInteraction.
   *
   * @param needGuiInteraction boolean if Gui Interaction is needed
   */
  public void setNeedGuiInteraction(boolean needGuiInteraction) {
    this.needGuiInteraction = needGuiInteraction;
  }

  /**
   * This method create a RobotInformation object containing important data for the gui.
   *
   * @return RobotInformation
   */
  public RobotInformation getRobotInformation() {
    return new RobotInformation(lives, position, direction, damage, currentCheckpoint, respawn);
  }

  // -------------------------------------------------------------------------------
  // Operations
  // -------------------------------------------------------------------------------

  /**
   * Updates and validates selected cards. Cards are only set to unblocked
   *
   * @param cards the new cards
   * @return boolean if cards are valid and updated
   */
  @SuppressWarnings({"java:S5413", "java:S3776"})
  public boolean updateSelectedCards(ProgrammingCard[] cards) {
    if (PROGRAM_SIZE < cards.length) {
      return false;
    }
    List<ProgrammingCard> hand =
        Arrays.stream(handCards).filter(Objects::nonNull).collect(Collectors.toList());
    ProgrammingCard[] updatedSelectedCards = new ProgrammingCard[PROGRAM_SIZE];
    if (isShutdown) {
      setSelectedCards(updatedSelectedCards);
    }
    for (int i = 0; i < cards.length; i++) {
      if (blocked[i]) {
        if (selectedCards[i].equals(cards[i])) {
          updatedSelectedCards[i] = selectedCards[i];
        } else {
          return false;
        }
      } else if (cards[i] != null) {
        if (hand.contains(cards[i])) {
          updatedSelectedCards[i] = cards[i];
          hand.remove(cards[i]);
        } else {
          return false;
        }
      }
    }
    // fill up randomly with hand cards
    Collections.shuffle(hand);
    for (int i = 0; i < PROGRAM_SIZE; i++) {
      if (updatedSelectedCards[i] == null) {
        updatedSelectedCards[i] = blocked[i] ? selectedCards[i] : hand.remove(0);
      }
    }
    setSelectedCards(updatedSelectedCards);
    return true;
  }

  /** Take damage. */
  public boolean damage() {
    if (++damage > MEMORY_SIZE) {
      return destroy();
    }
    if (nbDamagedProgramSlots() > nbBlockedProgramSlots()) {
      for (int i = blocked.length - 1; i >= 0; i--) {
        // block the first unblocked slot from the end of the ProgramSlots
        if (!blocked[i]) {
          blocked[i] = true;
          break;
        }
      }
    }
    return false;
  }

  /**
   * Repair execution. Returns amount of Slots to Unblock
   *
   * @param i the amount of removed damage
   */
  public int repair(int i) {
    if (i < 1 || i > 2) {
      throw new IllegalArgumentException("Illegal repair amount");
    }
    damage -= i;
    if (damage < 0) {
      damage = 0;
    }
    return Math.max(nbBlockedProgramSlots() - nbDamagedProgramSlots(), 0);
  }

  /** Loses a life. */
  public boolean destroy() {
    alive = false;
    if (--lives == 0) {
      position = null;
      return true;
    }
    position = respawn;
    resetDamage();
    return false;
  }

  //
  // Helper methods
  //

  private int nbDamagedProgramSlots() {
    return Math.max(damage - (MEMORY_SIZE - PROGRAM_SIZE), 0);
  }

  private int nbBlockedProgramSlots() {
    int count = 0;
    for (boolean b : blocked) {
      if (b) {
        count++;
      }
    }
    return count;
  }

  public FloorField getRespawnField() {
    return respawn;
  }

  /**
   * Returns a list of ProgrammingCard objects that are currently blocked.
   *
   * @return a List of ProgrammingCard objects representing the blocked cards
   */
  public List<ProgrammingCard> getBlockedCards() {
    List<ProgrammingCard> programmingCardList = new ArrayList<>();
    for (int i = 0; i < blocked.length; i++) {
      if (isBlocked(i)) {
        programmingCardList.add(selectedCards[i]);
      }
    }
    return programmingCardList;
  }

  /** Shuts the robot down by setting isShutdown to true. */
  public void shutdown() {
    damage = 0;
    isShutdown = true;
  }

  /**
   * Getter for the isShutdown parameter of the robot.
   *
   * @return boolean saying if the robot is shutdown
   */
  public boolean isShutdown() {
    return isShutdown;
  }

  /** Turns the robot back on and sets its damage to 0 if the robot is currently shut down. */
  public void turnOn() {
    if (isShutdown) {
      isShutdown = false;
    }
  }

  /**
   * Getter for the isNeedToShutdown parameter of the robot.
   *
   * @return if the robot needs to shutdown next Round
   */
  public boolean isNeedToShutdown() {
    return isNeedToShutdown;
  }

  /**
   * Setter for the isNeedToShutdown parameter of the robot.
   *
   * @param isNeedToShutdown if the robot needs to Shutdown next Round
   */
  public void setIsNeedToShutdown(boolean isNeedToShutdown) {
    this.isNeedToShutdown = isNeedToShutdown;
  }
}
