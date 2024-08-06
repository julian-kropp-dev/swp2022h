package de.uol.swp.common.lobby.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/**
 * This Exception is thrown when a user tries to join a lobby as a spectator but the lobby does not
 * allow spectators.
 */
public class LobbyDoesNotAllowSpectatorsExceptionMessage extends AbstractResponseMessage {

  private static final long serialVersionUID = -5134847438035487167L;
  private final String message;

  public LobbyDoesNotAllowSpectatorsExceptionMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "Die Lobby " + message + " hat den Zuschauermodus nicht eingeschaltet.";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyDoesNotAllowSpectatorsExceptionMessage that =
        (LobbyDoesNotAllowSpectatorsExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }
}
