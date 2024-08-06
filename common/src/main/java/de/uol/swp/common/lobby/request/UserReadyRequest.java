package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/** Class for UserReadyRequest. */
public class UserReadyRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = -4497488392449098142L;

  /**
   * Constructor.
   *
   * @param lobbyId id of the Lobby
   * @param user user who wants his ready status to be changed
   */
  public UserReadyRequest(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
