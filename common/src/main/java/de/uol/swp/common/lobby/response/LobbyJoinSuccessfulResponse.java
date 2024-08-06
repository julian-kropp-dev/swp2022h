package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * This response is sent to the Client whose LobbyJoinUserRequest was successful.
 *
 * @see de.uol.swp.common.lobby.request.LobbyJoinUserRequest
 * @see User
 */
public class LobbyJoinSuccessfulResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 6246212380588171588L;
  private final User user;
  private final String lobbyId;
  private final LobbyOptions lobbyOptions;

  /**
   * Constructor.
   *
   * @param user the user who successfully joined a lobby
   */
  public LobbyJoinSuccessfulResponse(String inLobbyId, User user, LobbyOptions lobbyOptions) {
    this.lobbyId = inLobbyId;
    this.user = user;
    this.lobbyOptions = lobbyOptions;
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
   * @return User object of the user who successfully joined a lobby
   */
  public User getUser() {
    return user;
  }

  public LobbyOptions getLobbyOptions() {
    return lobbyOptions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyJoinSuccessfulResponse that = (LobbyJoinSuccessfulResponse) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }
}
