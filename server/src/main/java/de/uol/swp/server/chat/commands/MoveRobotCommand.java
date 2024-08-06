package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.animation.AnimationType;
import de.uol.swp.common.game.animation.SingleAnimation;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;
import de.uol.swp.server.game.GameLogic;
import de.uol.swp.server.game.GameManagement;
import de.uol.swp.server.game.GameService;
import de.uol.swp.server.message.MoveRobotInternalMessage;
import java.util.Optional;

/**
 * This Class is for the move robot command /move.
 *
 * <p>Is handle the reaction from the ChatService to move the own robot with the variable: n: North
 * e, o: East, s: South, w: west,
 */
@SuppressWarnings("UnstableApiUsage")
public class MoveRobotCommand implements Command {

  private final EventBus eventBus;
  private Direction inputDirection;

  public MoveRobotCommand(EventBus eventBus) {
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
  @Override
  @SuppressWarnings({
    "java:S1854",
    "checkstyle:AbbreviationAsWordInName",
    "java:S3776",
    "UnusedAssignment"
  })
  public void execute(
      ChatMessageDTO chatMessageDTO,
      String command,
      String variable,
      String[][] parameter,
      ChatService chatService,
      MessageContext messageContext) {
    String returnString;
    boolean rotation;
    if (chatMessageDTO.getLobbyId().equals("GLOBAL")) {
      chatService.sendToClient(
          messageContext,
          chatMessageDTO.getLobbyId(),
          "Kann nur im Chat in einem Spiel ausgeführt werden.");
      return;
    }
    rotation = false;
    returnString = "Bewege den Roboter";
    GameManagement gameManagement = GameManagement.getInstance();

    Optional<Game> gameOptional = gameManagement.getGame(chatMessageDTO.getLobbyId());

    if (gameOptional.isEmpty()) {
      return;
    }
    GameDTO game = (GameDTO) gameOptional.get();
    Robot robot = game.getPlayer(chatMessageDTO.getUserDTO()).getRobot();
    GameService gameService = GameService.getInstance();

    GameLogic gameLogic = new GameLogic(game, gameService, eventBus);

    Direction direction = null;

    if (variable != null && !variable.equals("")) {
      switch (variable) {
        case "n":
          direction = Direction.NORTH;
          break;
        case "e":
          direction = Direction.EAST;
          break;
        case "s":
          direction = Direction.SOUTH;
          break;
        case "w":
          direction = Direction.WEST;
          break;
        case "r":
          direction = robot.getDirection().rotate(Direction.EAST);
          rotation = true;
          inputDirection = Direction.EAST;
          break;
        case "l":
          direction = robot.getDirection().rotate(Direction.WEST);
          rotation = true;
          inputDirection = Direction.WEST;
          break;
        case "u":
          direction = robot.getDirection().rotate(Direction.SOUTH);
          rotation = true;
          inputDirection = Direction.SOUTH;
          break;
        default:
          returnString = "Parameter konnte nicht gefunden werden";
          return;
      }
    }
    Step step = new Step(game.getGameId());
    FloorField position = robot.getPosition();
    if (direction != null && position.getNeighbour(direction) != null && !rotation) {
      if (parameter.length >= 2 && parameter[1][0].equals("logic")) {
        step.addAnimation(gameLogic.moveRobotExtern(robot, direction));
      } else {
        step.addAnimation(
            new SingleAnimation(
                AnimationType.getAnimationForDriving(direction),
                robot.getType(),
                robot.getPosition().getX(),
                robot.getPosition().getY(),
                robot.getDirection()));
        robot.setPosition(position.getNeighbour(direction));
      }
      if (robot.getPosition() != null) {
        eventBus.post(new MoveRobotInternalMessage(step, game.getLobby()));
      }
    } else if (rotation) {
      step.addAnimation(
          new SingleAnimation(
              AnimationType.getAnimationForTurning(inputDirection),
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              robot.getDirection()));
      robot.setDirection(direction);
      eventBus.post(new MoveRobotInternalMessage(step, game.getLobby()));
    } else {
      returnString = "Der Roboter konnte nicht bewegt werden!";
    }

    chatService.sendToClient(messageContext, chatMessageDTO.getLobbyId(), returnString);
  }
}
