package de.uol.swp.common.lobby.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/** This exception is thrown if something went wrong while updating the LobbyOptions. */
public class UpdateLobbyOptionsExceptionMessage extends AbstractResponseMessage {
  private static final long serialVersionUID = 9089101184478849597L;
  private final String message;
  private final String lobbyId;

  /**
   * Constructor.
   *
   * @param message String containing the reason why the Update failed
   * @param lobbyId the name of the lobby which update failed
   */
  public UpdateLobbyOptionsExceptionMessage(String message, String lobbyId) {
    this.message = message;
    this.lobbyId = lobbyId;
  }

  /**
   * Getter for the lobby name.
   *
   * @return the name of the lobby
   */
  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public String toString() {
    return "UpdateLobbyOptionsExceptionMessage " + message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateLobbyOptionsExceptionMessage that = (UpdateLobbyOptionsExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }
}
