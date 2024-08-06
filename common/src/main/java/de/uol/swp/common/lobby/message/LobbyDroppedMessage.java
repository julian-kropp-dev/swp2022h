package de.uol.swp.common.lobby.message;

import java.util.Objects;

/** Class for LobbyDroppedMessage. */
public class LobbyDroppedMessage extends AbstractLobbyMessage {
  private static final long serialVersionUID = 1931127425619418680L;

  /**
   * Constructor for LobbyDroppedMessage.
   *
   * @param name Name of the Lobby
   */
  public LobbyDroppedMessage(String name) {
    super.lobbyId = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyDroppedMessage that = (LobbyDroppedMessage) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }
}
