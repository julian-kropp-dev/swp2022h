package de.uol.swp.common.lobby.message;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/**
 * This message is sent to all clients in the lobby. It informs them that a user was kicked from the
 * lobby
 *
 * @see de.uol.swp.common.lobby.request.LobbyLeaveUserRequest
 * @see User
 */
public class KickedUserMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = 7571975935133572833L;

  /**
   * Constructor for KickedUserMessage.
   *
   * @param lobbyId Lobby in which User was kicked
   * @param user User who was kicked
   */
  public KickedUserMessage(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KickedUserMessage that = (KickedUserMessage) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }
}
