package de.uol.swp.server.chat;

import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.message.MessageContext;

/** This interface is used to create new commands. */
public interface Command {
  /**
   * This method is used when executing the command, everything we put in it is executed.
   *
   * @param chatMessageDTO The message of the command.
   * @param command The Command as a String.
   * @param variable The first variable that comes after the command, without having to specify the
   *     name of the variable.
   * @param parameter The parameters that are passed with the command.
   * @param chatService The chat service is used to send messages to the client.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  void execute(
      ChatMessageDTO chatMessageDTO,
      String command,
      String variable,
      String[][] parameter,
      ChatService chatService,
      MessageContext messageContext);
}
