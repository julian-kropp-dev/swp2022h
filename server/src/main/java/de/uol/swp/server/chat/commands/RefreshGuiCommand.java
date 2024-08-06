package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.message.RobotInformationMessage;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;
import de.uol.swp.server.game.GameManagement;
import java.util.Optional;

/** This Class is for the update the game gui /gui. */
@SuppressWarnings("UnstableApiUsage")
public class RefreshGuiCommand implements Command {

  private final EventBus eventBus;

  public RefreshGuiCommand(EventBus eventBus) {
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
    GameManagement gameManagement;
    if (chatMessageDTO.getLobbyId().equals("GLOBAL")) {
      chatService.sendToClient(
          messageContext,
          chatMessageDTO.getLobbyId(),
          "Kann nur im Chat in einem Spiel ausgeführt werden.");
      return;
    }
    gameManagement = GameManagement.getInstance();

    Optional<Game> optionalGameDTO = gameManagement.getGame(chatMessageDTO.getLobbyId());

    if (optionalGameDTO.isEmpty()) {
      return;
    }
    GameDTO game = (GameDTO) optionalGameDTO.get();
    game.getPlayers().stream()
        .map(Player::getRobot)
        .forEach(
            robot ->
                eventBus.post(
                    new RobotInformationMessage(
                        robot.getType(), robot.getRobotInformation(), game.getGameId())));

    chatService.sendToClient(messageContext, chatMessageDTO.getLobbyId(), "GUI Aktualisiert");
  }
}
