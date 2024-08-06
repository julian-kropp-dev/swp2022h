package de.uol.swp.common.lobby.message;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Class for UpdatedLobbyOptionsMessage. */
public class UpdatedLobbyOptionsMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = -4285272461529758233L;

  private LobbyOptions options;

  /**
   * Default constructor.
   *
   * @implNote needed for Serial
   */
  @SuppressWarnings("unused")
  public UpdatedLobbyOptionsMessage() {}

  /**
   * Constructor with name, user and options.
   *
   * @param name the lobby name
   * @param user the user who send the request
   * @param options the new options
   */
  public UpdatedLobbyOptionsMessage(String name, UserDTO user, LobbyOptions options) {
    super(name, user);
    this.options = options;
  }

  /**
   * Getter fot lobbyOptions.
   *
   * @return the LobbyOptions
   */
  public LobbyOptions getOptions() {
    return options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdatedLobbyOptionsMessage that = (UpdatedLobbyOptionsMessage) o;
    return Objects.equals(options, that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(options);
  }
}
