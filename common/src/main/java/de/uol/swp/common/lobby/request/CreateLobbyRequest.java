package de.uol.swp.common.lobby.request;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/**
 * Request sent to the server when a user wants to create a new lobby.
 *
 * @see de.uol.swp.common.lobby.request.AbstractLobbyRequest
 * @see de.uol.swp.common.user.User
 */
public class CreateLobbyRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = 1725155481539202092L;
  private LobbyOptions lobbyOptions;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public CreateLobbyRequest() {}

  /**
   * Constructor.
   *
   * @param name name of the lobby
   * @param owner User trying to create the lobby
   */
  public CreateLobbyRequest(String name, UserDTO owner, LobbyOptions options) {
    super(name, owner);
    this.lobbyOptions = options;
  }

  /**
   * Getter for the user variable.
   *
   * @return User trying to create the lobby
   */
  public User getOwner() {
    return getUser();
  }

  /**
   * Setter for the user variable.
   *
   * @param owner User trying to create the lobby
   */
  public void setOwner(UserDTO owner) {
    setUser(owner);
  }

  /**
   * Getter for the LobbyOptions.
   *
   * @return LobbyOptions
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
    CreateLobbyRequest that = (CreateLobbyRequest) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }
}
