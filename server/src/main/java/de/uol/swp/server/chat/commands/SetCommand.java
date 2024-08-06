package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.message.RobotInformationMessage;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;
import de.uol.swp.server.game.GameManagement;
import java.util.Optional;

/**
 * This Class is for set the robot to a location
 *
 * <p>Is handle the reaction from the ChatService to set damage the own robot with a variable amount
 * of damage.
 */
@SuppressWarnings("UnstableApiUsage")
public class SetCommand implements Command {

  private final EventBus eventBus;

  public SetCommand(EventBus eventBus) {
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
  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "java:S3776"})
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
          "Kann nur im Chat in einem Spiel ausgeführt werden.");
      return;
    }
    GameManagement gameManagement = GameManagement.getInstance();

    Optional<Game> optionalGame = gameManagement.getGame(chatMessageDTO.getLobbyId());

    if (optionalGame.isEmpty()) {
      return;
    }

    GameDTO gameDTO = (GameDTO) optionalGame.get();

    final FloorField[] field = {null};
    if (parameter.length >= 2 && parameter[1][0].equals("pos")) {
      try {
        field[0] =
            gameDTO
                .getFloorPlan()
                .getFloorFields(
                    Integer.parseInt(parameter[1][2]), Integer.parseInt(parameter[1][1]));
      } catch (ArrayIndexOutOfBoundsException
          | NumberFormatException arrayIndexOutOfBoundsException) {
        chatService.sendToClient(messageContext, chatMessageDTO.getLobbyId(), "Ungültiger Werte");
      }
    }

    if (parameter.length >= 3 && parameter[2][0].equals("robot")) {
      gameDTO
          .getPlayers()
          .forEach(
              player -> {
                Robot robot = player.getRobot();
                try {
                  if (robot.getType() == Robots.valueOf(parameter[2][1].toUpperCase())) {
                    if (field[0] == null) {
                      field[0] = robot.getPosition();
                    }
                    gameDTO.getPlayer(robot).getRobot().setPosition(field[0]);
                    chatService.sendToClient(
                        messageContext,
                        chatMessageDTO.getLobbyId(),
                        "Stelle den Roboter: " + parameter[2][1] + " da");
                    eventBus.post(
                        new RobotInformationMessage(
                            robot.getType(), robot.getRobotInformation(), gameDTO.getGameId()));
                  }
                } catch (IllegalArgumentException e) {
                  chatService.sendToClient(
                      messageContext, chatMessageDTO.getLobbyId(), "Ungültiger Werte");
                }
              });
    } else {
      if (field[0] == null) {
        field[0] = gameDTO.getPlayer(chatMessageDTO.getUserDTO()).getRobot().getPosition();
        chatService.sendToClient(
            messageContext, chatMessageDTO.getLobbyId(), "Setze den eigenen Roboter");
      }
      Robot robot = gameDTO.getPlayer(chatMessageDTO.getUserDTO()).getRobot();
      robot.setPosition(field[0]);
      eventBus.post(
          new RobotInformationMessage(
              robot.getType(), robot.getRobotInformation(), gameDTO.getGameId()));
    }
  }
}
