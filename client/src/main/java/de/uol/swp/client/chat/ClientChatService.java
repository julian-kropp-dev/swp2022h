package de.uol.swp.client.chat;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.chat.request.ChatMessageRequest;
import de.uol.swp.common.user.UserDTO;

/**
 * This class is responsible for the communication in the context of the chat between client and
 * server.
 */
@SuppressWarnings("UnstableApiUsage")
public class ClientChatService {

  private final EventBus bus;

  private final LoggedInUser loggedInUser;

  /**
   * The constructor of the class.
   *
   * @param bus The bus to write messages to the server.
   */
  @Inject
  public ClientChatService(EventBus bus, LoggedInUser loggedInUser) {
    this.bus = bus;
    bus.register(this);
    this.loggedInUser = loggedInUser;
  }

  /**
   * This function sends the message to the server.
   *
   * @param message is the message from the user.
   * @param userDTO is the logged-in user.
   * @param lobbyId the name of the lobby where this message is written.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void sendMessage(String message, UserDTO userDTO, String lobbyId) {
    bus.post(new ChatMessageRequest(new ChatMessageDTO(userDTO, message, lobbyId)));
  }

  /**
   * This function make it possible to write messages without having knowledge of the logged-in
   * user.
   *
   * @param message is the message from the user.
   * @param lobbyId the name of the lobby where this message is written.
   */
  public void sendMessage(String message, String lobbyId) {
    if (loggedInUser.getUser() != null) {
      bus.post(
          new ChatMessageRequest(new ChatMessageDTO(loggedInUser.getUser(), message, lobbyId)));
    }
  }
}
