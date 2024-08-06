package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/**
 * Request sent to the server when a user wants to leave a lobby.
 *
 * @see AbstractLobbyRequest
 * @see de.uol.swp.common.user.User
 */
public class LobbyLeaveUserRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = -7751549889425488691L;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public LobbyLeaveUserRequest() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param user user who wants to leave the lobby
   */
  public LobbyLeaveUserRequest(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
