package de.uol.swp.common.game;

import java.io.Serializable;

/** Class containing all options for the game. */
public class GameOptions implements Serializable {
  private static final long serialVersionUID = -650722345664991052L;
  private GamePhase gamePhase;

  /** Default constructor with default values. All Option are set to their default value. */
  public GameOptions() {
    gamePhase = GamePhase.UPGRADE_PHASE;
  }

  /**
   * Getter for the GamePhase ENUM.
   *
   * @return GamePhase
   */
  public GamePhase getGamePhase() {
    return gamePhase;
  }

  /**
   * Setter for the GamePhase.
   *
   * @param gamePhase GamePhase to set
   */
  public void setGamePhase(GamePhase gamePhase) {
    this.gamePhase = gamePhase;
  }

  /**
   * Return wether it's the last phase or not.
   *
   * @param gamePhase actual game phase to check
   * @return boolean return wether it's the last phase or not
   */
  public boolean isLastPhase(GamePhase gamePhase) {
    return gamePhase == GamePhase.VERIFICATION_PHASE;
  }

  /**
   * Switch to the next game phase.
   *
   * @throws IllegalArgumentException if an unknown game phase is passed
   */
  public void nextPhase() {
    switch (this.gamePhase) {
      case UPGRADE_PHASE:
        setGamePhase(GamePhase.PROGRAMMING_PHASE);
        break;
      case PROGRAMMING_PHASE:
        setGamePhase(GamePhase.ROBOT_MOVEMENT_PHASE);
        break;
      case ROBOT_MOVEMENT_PHASE:
        setGamePhase(GamePhase.VERIFICATION_PHASE);
        break;
      case VERIFICATION_PHASE:
        setGamePhase(GamePhase.UPGRADE_PHASE);
        break;
      default:
        throw new IllegalArgumentException("Unknown game phase");
    }
  }

  /** Specifies the status of the game (ENUM). */
  public enum GamePhase implements Serializable {
    UPGRADE_PHASE("Austeilphase"),
    PROGRAMMING_PHASE("Programmierphase"),
    ROBOT_MOVEMENT_PHASE("Programmablauf"),
    VERIFICATION_PHASE("Abschluss der Runde");

    private final String text;

    GamePhase(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
