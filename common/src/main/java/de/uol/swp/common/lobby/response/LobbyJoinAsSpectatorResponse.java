package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/** This response is sent to the Client whose LobbyJoinUserRequest was successful. */
public class LobbyJoinAsSpectatorResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 1398233710930465491L;
  private final User user;
  private final String lobbyId;
  private final LobbyOptions lobbyOptions;
  private final LobbyStatus lobbyStatus;

  /**
   * Constructor.
   *
   * @param user the user who successfully joined a lobby
   */
  public LobbyJoinAsSpectatorResponse(
      String inLobbyId, User user, LobbyOptions lobbyOptions, LobbyStatus lobbyStatus) {
    this.lobbyId = inLobbyId;
    this.user = user;
    this.lobbyOptions = lobbyOptions;
    this.lobbyStatus = lobbyStatus;
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

  public LobbyStatus getLobbyStatus() {
    return lobbyStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyJoinAsSpectatorResponse that = (LobbyJoinAsSpectatorResponse) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }
}
