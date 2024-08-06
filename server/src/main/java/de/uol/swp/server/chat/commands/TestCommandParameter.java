package de.uol.swp.server.chat.commands;

import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;

/** Class for TestCommandParameter. */
public class TestCommandParameter implements Command {

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
    String text = null;
    for (String[] strings : parameter) {
      if (strings[0].equals("text")) {
        text = strings[1];
      }
    }
    if (text != null) {
      chatService.sendToClient(messageContext, chatMessageDTO.getLobbyId(), text);
    }
  }
}
