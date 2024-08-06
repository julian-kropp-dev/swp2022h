package de.uol.swp.common.lobby.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/** This exception is thrown if a user tried to join a Lobby with a wrong LobbyStatus. */
public class LobbyNotWaitingAnymoreExceptionMessage extends AbstractResponseMessage {
  private static final long serialVersionUID = -657158876380585199L;
  private final String message;

  /**
   * Constructor.
   *
   * @param message String containing why the lobby join failed
   */
  public LobbyNotWaitingAnymoreExceptionMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "LobbyNotWaitingAnymoreExceptionMessage" + message;
  }

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
    LobbyNotWaitingAnymoreExceptionMessage that = (LobbyNotWaitingAnymoreExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), message);
  }
}
