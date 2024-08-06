package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/** Class for ReadyCheckRequest. */
public class ReadyCheckRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = -8490142727039578956L;

  /**
   * Constructor.
   *
   * @param lobbyId in which lobby the ready list should be checked.
   * @param user user who wants to initiate a ReadyCheckRequest.
   */
  public ReadyCheckRequest(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
