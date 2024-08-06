package de.uol.swp.common.lobby.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/**
 * This exception is thrown if something went wrong during the leave lobby process. e.g.: User is
 * not in lobby.
 */
public class LobbyLeaveExceptionMessage extends AbstractResponseMessage {

  private static final long serialVersionUID = 1038595571893722957L;
  private final String message;

  /**
   * Constructor.
   *
   * @param message String containing the reason why the LobbyLeave failed
   */
  public LobbyLeaveExceptionMessage(String message) {
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
    LobbyLeaveExceptionMessage that = (LobbyLeaveExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }
}
