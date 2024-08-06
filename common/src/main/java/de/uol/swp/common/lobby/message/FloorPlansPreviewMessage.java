package de.uol.swp.common.lobby.message;

import de.uol.swp.common.user.UserDTO;

/** Message that will be sent to all members of the lobby when FloorPlans are updated. */
public class FloorPlansPreviewMessage extends AbstractLobbyMessage {
  private static final long serialVersionUID = -5203619609037211013L;

  /**
   * Constructor for FloorPlansPreviewMessage.
   *
   * @param lobbyId Lobby
   * @param user User
   */
  public FloorPlansPreviewMessage(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
