package de.uol.swp.common.game.request;

import de.uol.swp.common.lobby.request.AbstractLobbyRequest;
import de.uol.swp.common.user.UserDTO;

/** A request class for programming cards in a lobby. Extends the AbstractLobbyRequest class. */
public class ProgrammingCardRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = -7151449485287102118L;

  /**
   * Constructs a ProgrammingCardRequest with the specified name and UserDTO.
   *
   * @param name the name of the request
   * @param user the UserDTO associated with the request
   */
  public ProgrammingCardRequest(String name, UserDTO user) {
    super(name, user);
  }
}
