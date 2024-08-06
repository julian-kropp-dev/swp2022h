package de.uol.swp.common.lobby.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * This response is sent to the Client whose LobbyLeaveUserRequest was successful.
 *
 * @see de.uol.swp.common.lobby.request.LobbyLeaveUserRequest
 * @see User
 */
public class LobbyLeaveSuccessfulResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 622345680588171588L;
  private final User user;
  private final String lobbyId;

  /**
   * Constructor.
   *
   * @param user the user who successfully left a lobby
   */
  public LobbyLeaveSuccessfulResponse(String isLobbyId, User user) {
    this.lobbyId = isLobbyId;
    this.user = user;
  }

  /**
   * Getter for the lobbyId variable.
   *
   * @return lobbyId as String
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Getter for the User variable.
   *
   * @return User object of the user who successfully left a lobby
   */
  public User getUser() {
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyLeaveSuccessfulResponse that = (LobbyLeaveSuccessfulResponse) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }
}
