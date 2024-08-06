package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/** Request sent to the server when a spectator wants to join a lobby. */
public class LobbyJoinAsSpectatorRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = 2515077578358895777L;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public LobbyJoinAsSpectatorRequest() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param user user who wants to join the lobby
   */
  public LobbyJoinAsSpectatorRequest(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
