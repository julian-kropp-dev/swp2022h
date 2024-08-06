package de.uol.swp.common.lobby.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/**
 * Response with the current owner of the lobby.
 *
 * @see AbstractResponseMessage
 * @see de.uol.swp.common.lobby.dto.LobbyDTO
 * @see de.uol.swp.common.lobby.request.LobbyOwnerRequest
 */
public class LobbyOwnerResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = -1527394303892251066L;
  private final UserDTO owner;
  private final String lobbyId;

  /**
   * Constructor to set owner.
   *
   * @param lobbyId lobbyId of the owner
   * @param owner owner of the Lobby
   */
  public LobbyOwnerResponse(String lobbyId, UserDTO owner) {
    this.lobbyId = lobbyId;
    this.owner = owner;
  }

  /**
   * Getter for the owner of the Lobby.
   *
   * @return Owner of the lobby
   */
  public UserDTO getOwner() {
    return this.owner;
  }

  /**
   * Getter for the id of the Lobby.
   *
   * @return name of lobby
   */
  public String getLobbyId() {
    return this.lobbyId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyOwnerResponse that = (LobbyOwnerResponse) o;
    return Objects.equals(owner, that.owner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner);
  }
}
