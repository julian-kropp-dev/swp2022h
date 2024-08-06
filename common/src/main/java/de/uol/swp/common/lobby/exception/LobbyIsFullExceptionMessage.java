package de.uol.swp.common.lobby.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/** This exception is thrown when a user tries to join a full lobby. */
public class LobbyIsFullExceptionMessage extends AbstractResponseMessage {

  private static final long serialVersionUID = -5525239393273417829L;
  private final String message;

  /**
   * Constructor.
   *
   * @param message String containing why the lobby join failed
   */
  public LobbyIsFullExceptionMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "LobbyIsFullExceptionMessage" + message;
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
    LobbyIsFullExceptionMessage that = (LobbyIsFullExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), message);
  }
}
