package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.request.ProgrammingCardRequest;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;

/** Test Command for Requesting Cards. */
@SuppressWarnings("UnstableApiUsage")
public class GetCardsCommand implements Command {
  private final EventBus eventBus;

  public GetCardsCommand(EventBus eventBus) {
    this.eventBus = eventBus;
  }

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
          "Kann nur im Chat einer Lobby ausgeführt werden.");
      return;
    }
    chatService.sendToClient(
        messageContext,
        chatMessageDTO.getLobbyId(),
        "Sende Karten Request für " + chatMessageDTO.getLobbyId());
    eventBus.post(
        new ProgrammingCardRequest(chatMessageDTO.getLobbyId(), chatMessageDTO.getUserDTO()));
  }
}
