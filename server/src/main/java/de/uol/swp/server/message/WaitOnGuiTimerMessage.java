package de.uol.swp.server.message;

import java.util.Objects;

/**
 * Represents a message indicating that a game GUI has finished. This message is used for internal
 * server communication.
 */
public class WaitOnGuiTimerMessage extends AbstractServerInternalMessage {
  private static final long serialVersionUID = -5197889810987088L;

  /** The name of the game for which the GUI has finished. */
  private final String gameId;

  /**
   * Constructs a new WaitOnGuiTimerMessage with the specified game name.
   *
   * @param gameId the name of the game for which the GUI has finished
   */
  public WaitOnGuiTimerMessage(String gameId) {
    super();
    this.gameId = gameId;
  }

  /**
   * Returns the name of the game for which the GUI has finished.
   *
   * @return the name of the game for which the GUI has finished
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Returns a string representation of this WaitOnGuiTimerMessage object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return "WaitOnGuiTimerMessage{gameName='" + gameId + "'}";
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
    WaitOnGuiTimerMessage that = (WaitOnGuiTimerMessage) o;
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
