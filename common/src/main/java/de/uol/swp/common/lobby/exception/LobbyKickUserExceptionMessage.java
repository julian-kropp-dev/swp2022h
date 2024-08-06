package de.uol.swp.common.lobby.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/**
 * This exception is thrown if something went wrong while kicking a user from a lobby. e.g.: User is
 * not in lobby
 */
public class LobbyKickUserExceptionMessage extends AbstractResponseMessage {

  private static final long serialVersionUID = 6545330160897497074L;
  private final String message;

  /**
   * Constructor.
   *
   * @param message String containing the reason why the LobbyLeave failed
   */
  public LobbyKickUserExceptionMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "LobbyLeaveExceptionMessage " + message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyKickUserExceptionMessage that = (LobbyKickUserExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }
}
