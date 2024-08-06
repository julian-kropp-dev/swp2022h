package de.uol.swp.common.lobby.request;

import de.uol.swp.common.user.UserDTO;

/** Class for FloorPlansPreviewRequest. */
public class FloorPlansPreviewRequest extends AbstractLobbyRequest {
  private static final long serialVersionUID = 3171153601778136287L;

  /** Constructor. */
  public FloorPlansPreviewRequest(String lobby, UserDTO user) {
    super(lobby, user);
  }
}
