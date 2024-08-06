package de.uol.swp.common.lobby.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** This response sent the ReadyCheckResponse back to the users in the lobby. */
public class ReadyCheckResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 523347770588171588L;
  private final boolean check;
  private final String lobbyId;
  private final User owner;

  /**
   * Constructor.
   *
   * @param lobbyId in which lobby the ready check was made
   * @param check boolean if all Users in lobby are ready (true if all users are ready)
   * @param owner lobby owner
   */
  public ReadyCheckResponse(String lobbyId, UserDTO owner, Boolean check) {
    this.check = check;
    this.owner = owner;
    this.lobbyId = lobbyId;
  }

  /**
   * Getter for the Check of the lobby.
   *
   * @return returns if all users in the lobby are ready or not (true if all users are ready).
   */
  public boolean getCheck() {
    return check;
  }

  /**
   * Getter for the Owner of the lobby.
   *
   * @return returns Owner of the lobby
   */
  public User getOwner() {
    return owner;
  }

  /**
   * Getter for the id of the Lobby.
   *
   * @return returns id of the Lobby
   */
  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReadyCheckResponse that = (ReadyCheckResponse) o;
    return Objects.equals(check, that.check);
  }

  @Override
  public int hashCode() {
    return Objects.hash(check);
  }
}
