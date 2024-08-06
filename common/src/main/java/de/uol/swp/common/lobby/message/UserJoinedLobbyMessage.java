package de.uol.swp.common.lobby.message;

import de.uol.swp.common.user.UserDTO;

/**
 * Message sent by the server when a user successfully joins a lobby.
 *
 * @see de.uol.swp.common.lobby.message.AbstractLobbyMessage
 * @see de.uol.swp.common.user.User
 */
public class UserJoinedLobbyMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = 3510833656242166078L;

  /**
   * Default constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public UserJoinedLobbyMessage() {}

  /**
   * Constructor.
   *
   * @param lobbyId name of the lobby
   * @param user user who joined the lobby
   */
  public UserJoinedLobbyMessage(String lobbyId, UserDTO user) {
    super(lobbyId, user);
  }
}
