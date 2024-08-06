package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Class for RetrieveLobbyOptionsResponse. */
public class RetrieveLobbyOptionsResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 604277735598766479L;
  private LobbyOptions options;
  private Map<User, Boolean> ready = new HashMap<>();
  private String lobbyId;
  private Set<User> users;
  private Set<User> spectators;

  /**
   * Constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public RetrieveLobbyOptionsResponse() {}

  /**
   * Constructor which requires the lobby name and its options.
   *
   * @param options options of the lobby
   * @param lobbyId name of the lobby
   */
  public RetrieveLobbyOptionsResponse(
      LobbyOptions options,
      String lobbyId,
      Set<User> user,
      Map<User, Boolean> ready,
      Set<User> spectators) {
    this.options = options;
    this.lobbyId = lobbyId;
    this.users = user;
    this.ready = ready;
    this.spectators = spectators;
  }

  /**
   * Getter for the lobby options.
   *
   * @return the lobby options
   */
  public LobbyOptions getOptions() {
    return options;
  }

  /**
   * Getter for the lobby id.
   *
   * @return the lobby id
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Getter for all users in lobby.
   *
   * @return the users.
   */
  public Set<User> getUsers() {
    return users;
  }

  /**
   * Getter for the spectators in lobby.
   *
   * @return the spectators
   */
  public Set<User> getSpectators() {
    return spectators;
  }

  /**
   * ready map of all user.
   *
   * @return all users are ready or not.
   */
  public Map<User, Boolean> getReady() {
    return ready;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RetrieveLobbyOptionsResponse that = (RetrieveLobbyOptionsResponse) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }
}
