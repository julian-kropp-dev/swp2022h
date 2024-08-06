package de.uol.swp.common.lobby.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/** This Exception is thrown when a user tries to join a lobby that does not exist. */
public class LobbyDoesNotExistExceptionMessage extends AbstractResponseMessage {

  private static final long serialVersionUID = -3953402821185576884L;

  private final String message;

  public LobbyDoesNotExistExceptionMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "Die Lobby " + message + " existiert nicht.";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyDoesNotExistExceptionMessage that = (LobbyDoesNotExistExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }
}
