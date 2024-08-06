package de.uol.swp.server.message;

import java.util.Objects;

/**
 * Represents a message to start the Respawn process if a robot was destroyed in the last round.
 * This message is used for internal server communication.
 */
public class StartRespawnInteractionInternalMessage extends AbstractServerInternalMessage {

  private static final long serialVersionUID = 2055857081998536842L;
  /** The name of the game to start the Respawn process. */
  private final String gameId;

  /**
   * Constructs a new StartRespawnInteractionInternalMessage with the specified game name.
   *
   * @param gameId the name of the game for which the Respawn process has to start
   */
  public StartRespawnInteractionInternalMessage(String gameId) {
    super();
    this.gameId = gameId;
  }

  /**
   * Getter for the game name.
   *
   * @return gameId The name of the game for which the Respawn process has to start
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Returns a string representation of this StartRespawnInteractionInternalMessage object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return "StartRespawnInteractionInternalMessage{gameName='" + gameId + "'}";
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @param o the object with which to compare
   * @return true if this object is the same as the o argument; false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    StartRespawnInteractionInternalMessage that = (StartRespawnInteractionInternalMessage) o;
    return Objects.equals(gameId, that.gameId);
  }

  /**
   * Returns a hash code value for this object.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), gameId);
  }
}
