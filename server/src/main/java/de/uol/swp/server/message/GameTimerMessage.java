package de.uol.swp.server.message;

import java.util.Objects;

/**
 * Represents a message indicating that a game timer has elapsed. This message is used for internal
 * server communication.
 */
public class GameTimerMessage extends AbstractServerInternalMessage {

  private static final long serialVersionUID = 5888144949892730227L;
  /** The name of the game for which the timer has elapsed. */
  private final String lobbyId;

  /**
   * Constructs a new GameTimerMessage with the specified game name.
   *
   * @param lobbyId the name of the game for which the timer has elapsed
   */
  public GameTimerMessage(String lobbyId) {
    super();
    this.lobbyId = lobbyId;
  }

  /**
   * Returns the name of the game for which the timer has elapsed.
   *
   * @return the name of the game for which the timer has elapsed
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Returns a string representation of this GameTimerMessage object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return "GameTimerMessage{gameName='" + lobbyId + "'}";
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
    GameTimerMessage that = (GameTimerMessage) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  /**
   * Returns a hash code value for this object.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lobbyId);
  }
}
