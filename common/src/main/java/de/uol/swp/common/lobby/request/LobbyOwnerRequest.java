package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/**
 * Request for getting Information about and for the owner of the lobby.
 *
 * @see AbstractLobbyRequest
 * @see de.uol.swp.common.lobby.response.LobbyOwnerResponse
 */
public class LobbyOwnerRequest extends AbstractLobbyRequest {

  /**
   * Request to get the owner of the lobby.
   *
   * @see de.uol.swp.common.message.AbstractRequestMessage
   */
  private static final long serialVersionUID = 5398222710930465491L;

  public LobbyOwnerRequest(String lobby, UserDTO user) {
    super(lobby, user);
  }
}
