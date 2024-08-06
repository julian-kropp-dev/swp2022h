package de.uol.swp.common.chat;

import de.uol.swp.common.user.UserDTO;
import java.io.Serializable;
import java.util.Objects;

/** This class represents a chat message. */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ChatMessageDTO implements Serializable, Comparable<ChatMessageDTO> {

  private static final long serialVersionUID = -4309979720041486125L;
  private final UserDTO userDTO;
  private final String message;
  private final String lobbyId;

  /**
   * Constructor of the class.
   *
   * @param userDTO The user of the message.
   * @param message The Message.
   * @param lobbyId In which lobby the message was written.
   */
  public ChatMessageDTO(UserDTO userDTO, String message, String lobbyId) {
    this.message = message.strip();
    this.userDTO = userDTO;
    this.lobbyId = lobbyId;
  }

  /**
   * returns the message.
   *
   * @return the massage String.
   */
  public String getMessage() {
    return message;
  }

  /**
   * returns the user.
   *
   * @return the user that send the message.
   */
  public UserDTO getUserDTO() {
    return userDTO;
  }

  /**
   * returns the lobby name.
   *
   * @return the name of the Lobby where the message is written
   */
  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public int compareTo(ChatMessageDTO o) {
    return userDTO.compareTo(o.userDTO) + message.compareTo(o.message);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChatMessageDTO that = (ChatMessageDTO) o;
    return userDTO.equals(that.userDTO)
        && message.equals(that.message)
        && lobbyId.equals(that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userDTO, message, lobbyId);
  }

  /**
   * This function returns the message with the author as a string. So that they can be displayed
   * directly in chat.
   *
   * @return (Username: message) as String
   */
  @Override
  public String toString() {
    return ((userDTO == null) ? "System" : userDTO.getUsername()) + ": " + message;
  }
}
