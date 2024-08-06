package de.uol.swp.common.lobby.message;

import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Class for UserReadyMessage. */
public class UserReadyMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = -8982566222989778289L;
  private final boolean readyStatus;

  /**
   * Constructor.
   *
   * @param lobbyId Ids of the Lobby
   * @param user user who changes his readyStatus
   */
  public UserReadyMessage(String lobbyId, UserDTO user, boolean readyStatus) {
    super(lobbyId, user);
    this.readyStatus = readyStatus;
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
    UserReadyMessage that = (UserReadyMessage) o;
    return readyStatus == that.readyStatus;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), readyStatus);
  }

  public boolean getReady() {
    return readyStatus;
  }
}
