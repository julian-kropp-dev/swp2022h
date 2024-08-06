package de.uol.swp.common.lobby.request;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Class for UpdateLobbyOptionsRequest. */
public class UpdateLobbyOptionsRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = 678553989557398706L;

  private LobbyOptions options;

  /** Default constructor. */
  @SuppressWarnings("unused")
  public UpdateLobbyOptionsRequest() {}

  /**
   * Constructor.
   *
   * @param lobbyId the lobbyId of the lobby
   * @param user the user who sends the request
   * @param options the new options
   */
  public UpdateLobbyOptionsRequest(String lobbyId, UserDTO user, LobbyOptions options) {
    super(lobbyId, user);
    this.options = options;
  }

  /**
   * Getter for the options.
   *
   * @return options
   */
  public LobbyOptions getOptions() {
    return this.options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateLobbyOptionsRequest that = (UpdateLobbyOptionsRequest) o;
    return Objects.equals(options, that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(options);
  }
}
