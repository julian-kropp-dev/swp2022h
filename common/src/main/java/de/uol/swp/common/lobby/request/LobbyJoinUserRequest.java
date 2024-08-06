package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/**
 * Request sent to the server when a user wants to join a lobby.
 *
 * @see AbstractLobbyRequest
 * @see de.uol.swp.common.user.User
 */
public class LobbyJoinUserRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = -1326595852402600485L;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public LobbyJoinUserRequest() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param user user who wants to join the lobby
   */
  public LobbyJoinUserRequest(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
