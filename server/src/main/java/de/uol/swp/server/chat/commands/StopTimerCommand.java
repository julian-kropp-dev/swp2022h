package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.request.StopTimerRequest;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;

/**
 * This class is for the /stopTimer command, which stops the timer of the current card selection
 * process.
 */
@SuppressWarnings("UnstableApiUsage")
public class StopTimerCommand implements Command {
  private final EventBus eventBus;

  public StopTimerCommand(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /**
   * This method is used when executing the command, everything we put in it is executed.
   *
   * @param chatMessageDTO The message of the command.
   * @param command The Command as a String.
   * @param variable The first variable that comes after the command, without having to specify the
   *     name of the variable.
   * @param parameter The parameters that are passed with the command.
   * @param chatService The chat service is used to send messages to the client.
   * @param messageContext messageContext
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Override
  public void execute(
      ChatMessageDTO chatMessageDTO,
      String command,
      String variable,
      String[][] parameter,
      ChatService chatService,
      MessageContext messageContext) {
    if (chatMessageDTO.getLobbyId().equals("GLOBAL")) {
      chatService.sendToClient(
          messageContext,
          chatMessageDTO.getLobbyId(),
          "Kann nur im Chat eines Spiels ausgeführt werden.");
      return;
    }
    chatService.sendToClient(
        messageContext,
        chatMessageDTO.getLobbyId(),
        "Sende StopTimer-Request für " + chatMessageDTO.getLobbyId());
    eventBus.post(
        new StopTimerRequest(
            chatMessageDTO.getUserDTO().getUsername(), chatMessageDTO.getLobbyId()));
  }
}
