package de.uol.swp.common.lobby.request;

import java.util.Objects;

/** Class for LobbyKickUserRequest. */
public class LobbyKickUserRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = -3713024546720180624L;
  private String username;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public LobbyKickUserRequest() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param username Username of user who gets kicked
   */
  public LobbyKickUserRequest(String lobbyId, String username) {
    this.lobbyId = lobbyId;
    this.username = username;
  }

  @Override
  public String getLobbyId() {
    return lobbyId;
  }

  public String getUsername() {
    return username;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyKickUserRequest that = (LobbyKickUserRequest) o;
    return Objects.equals(lobbyId, that.lobbyId) && Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lobbyId, username);
  }
}
