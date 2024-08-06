package de.uol.swp.common.lobby.message;

import de.uol.swp.common.user.UserDTO;

/**
 * Message sent by the server when a user successfully leaves a lobby.
 *
 * @see de.uol.swp.common.lobby.message.AbstractLobbyMessage
 * @see de.uol.swp.common.user.User
 */
public class UserLeftLobbyMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = 8771084596103673764L;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  public UserLeftLobbyMessage() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param user user who left the lobby
   */
  public UserLeftLobbyMessage(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
