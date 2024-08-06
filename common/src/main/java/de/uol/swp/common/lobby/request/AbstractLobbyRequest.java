package de.uol.swp.common.lobby.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/**
 * Base class of all lobby request messages. Basic handling of lobby data.
 *
 * @see de.uol.swp.common.user.User
 * @see de.uol.swp.common.message.AbstractRequestMessage
 */
public class AbstractLobbyRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 4123555857507512645L;
  String lobbyId;
  UserDTO user;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  public AbstractLobbyRequest() {}

  /**
   * Constructor.
   *
   * @param lobbyId lobbyId of the lobby
   * @param user user responsible for the creation of this message
   */
  public AbstractLobbyRequest(String lobbyId, UserDTO user) {
    this.lobbyId = lobbyId;
    this.user = user;
  }

  /**
   * Getter for the name variable.
   *
   * @return String containing the lobby's name
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Getter for the user variable.
   *
   * @return User responsible for the creation of this message
   */
  public UserDTO getUser() {
    return user;
  }

  /**
   * Setter for the user variable.
   *
   * @param user User responsible for the creation of this message
   */
  public void setUser(UserDTO user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractLobbyRequest that = (AbstractLobbyRequest) o;
    return Objects.equals(lobbyId, that.lobbyId) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId, user);
  }
}
