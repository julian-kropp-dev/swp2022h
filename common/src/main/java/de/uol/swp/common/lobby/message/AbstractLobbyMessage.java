package de.uol.swp.common.lobby.message;

import de.uol.swp.common.message.AbstractServerMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/**
 * Base class of all lobby messages. Basic handling of lobby data.
 *
 * @see de.uol.swp.common.user.User
 * @see de.uol.swp.common.message.AbstractServerMessage
 */
public class AbstractLobbyMessage extends AbstractServerMessage {

  private static final long serialVersionUID = -462377817911103429L;
  protected String lobbyId;
  protected UserDTO user;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  public AbstractLobbyMessage() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param user user responsible for the creation of this message
   */
  public AbstractLobbyMessage(String lobbyId, UserDTO user) {
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
  public User getUser() {
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
    AbstractLobbyMessage that = (AbstractLobbyMessage) o;
    return Objects.equals(lobbyId, that.lobbyId) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId, user);
  }
}
