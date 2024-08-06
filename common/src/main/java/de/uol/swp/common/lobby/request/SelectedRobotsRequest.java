package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/** Request for getting Information about and for the selected robots of the lobby. */
public class SelectedRobotsRequest extends AbstractLobbyRequest {

  /**
   * Request to get the selected robots of the lobby.
   *
   * @see de.uol.swp.common.message.AbstractRequestMessage
   */
  private static final long serialVersionUID = 5398222639740465491L;

  public SelectedRobotsRequest(String lobby, UserDTO user) {
    super(lobby, user);
  }
}
