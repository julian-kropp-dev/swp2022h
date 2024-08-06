package de.uol.swp.server.message;

import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Internal Message when user leaves a Lobby to remove him from Games. */
public class PlayerLeftLobbyMessage extends AbstractServerInternalMessage {
  private static final long serialVersionUID = -7751549883725488691L;
  private final Lobby lobby;
  private final UserDTO user;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  public PlayerLeftLobbyMessage(Lobby lobby, UserDTO user) {
    this.lobby = lobby;
    this.user = user;
  }

  /**
   * Returns the Lobby object representing the lobby in which the user left.
   *
   * @return The Lobby object representing the lobby in which the user left.
   */
  public Lobby getLobby() {
    return lobby;
  }

  /**
   * Returns the UserDTO object representing the user which wants to leave.
   *
   * @return The userdto object
   */
  public UserDTO getUser() {
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    PlayerLeftLobbyMessage that = (PlayerLeftLobbyMessage) o;
    return Objects.equals(lobby, that.lobby) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lobby, user);
  }
}
