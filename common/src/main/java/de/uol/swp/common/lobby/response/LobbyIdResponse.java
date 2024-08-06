package de.uol.swp.common.lobby.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/**
 * Response with the unique generated lobby name.
 *
 * @see AbstractResponseMessage
 */
public class LobbyIdResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 5895999713443142780L;
  private final String id;

  /**
   * Constructor to set the lobbyId.
   *
   * @param lobbyId the lobbyId of the Lobby
   */
  public LobbyIdResponse(String lobbyId) {
    this.id = lobbyId;
  }

  /**
   * Getter for the ID of the Lobby.
   *
   * @return Name of the Lobby
   */
  public String getLobbyId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyIdResponse that = (LobbyIdResponse) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
