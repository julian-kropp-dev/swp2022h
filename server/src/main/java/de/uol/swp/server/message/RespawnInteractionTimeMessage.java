package de.uol.swp.server.message;

import java.util.Objects;

/**
 * Represents a message indicating that a respawn interaction timer has elapsed. This message is
 * used for internal server communication.
 */
public class RespawnInteractionTimeMessage extends AbstractServerInternalMessage {

  private static final long serialVersionUID = 4622950751374856645L;

  /** The name of the game for which the timer has elapsed. */
  private final String gameId;

  /**
   * Constructs a new RespawnInteractionTimeMessage with the specified game name.
   *
   * @param gameId the name of the game for which the timer has elapsed
   */
  public RespawnInteractionTimeMessage(String gameId) {
    super();
    this.gameId = gameId;
  }

  /**
   * Getter for the game name.
   *
   * @return gameId The name of the game for which the timer has elapsed
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Returns a string representation of this RespawnInteractionTimeMessage object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return "RespawnInteractionTimeMessage{gameName='" + gameId + "'}";
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
    RespawnInteractionTimeMessage that = (RespawnInteractionTimeMessage) o;
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
