package de.uol.swp.common.lobby.message;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Message sent by the server when a user successfully joins a lobby. */
public class ChangedSpectatorModeMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = -6231894902331251797L;

  private boolean spectatorMode;

  private User owner;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public ChangedSpectatorModeMessage() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param user user who changed spectatorMode
   */
  public ChangedSpectatorModeMessage(
      String lobbyId, UserDTO user, boolean spectatorMode, User owner) {
    super(lobbyId, user);
    this.spectatorMode = spectatorMode;
    this.owner = owner;
  }

  /**
   * Getter for spectatorMode.
   *
   * @return value if the user changed to spectator
   */
  public boolean isSpectatorMode() {
    return spectatorMode;
  }

  /**
   * Getter for LobbyOwner.
   *
   * @return the LobbyOwner
   */
  public User getOwner() {
    return owner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChangedSpectatorModeMessage that = (ChangedSpectatorModeMessage) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }
}
