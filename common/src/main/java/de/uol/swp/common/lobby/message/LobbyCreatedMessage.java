package de.uol.swp.common.lobby.message;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Class for LobbyCreatedMessage. */
public class LobbyCreatedMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = 5746132548562871848L;
  private final LobbyOptions lobbyOptions;

  /**
   * Constructor with lobby options.
   *
   * @param lobbyId the lobby lobbyId
   * @param user the creator
   * @param lobbyOptions the lobbyOptions
   */
  public LobbyCreatedMessage(String lobbyId, UserDTO user, LobbyOptions lobbyOptions) {
    super(lobbyId, user);
    this.lobbyOptions = lobbyOptions;
  }

  /**
   * Getter for LobbyOptions.
   *
   * @return lobbyOptions
   */
  public LobbyOptions getLobbyOptions() {
    return lobbyOptions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyCreatedMessage that = (LobbyCreatedMessage) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }
}
