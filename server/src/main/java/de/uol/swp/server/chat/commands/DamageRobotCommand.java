package de.uol.swp.server.chat.commands;

import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.animation.AnimationType;
import de.uol.swp.common.game.animation.SingleAnimation;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;
import de.uol.swp.server.game.GameManagement;
import java.util.Optional;

/**
 * This Class is for the move robot command /damage.
 *
 * <p>Is handle the reaction from the ChatService to set damage the own robot with a variable amount
 * of damage.
 */
public class DamageRobotCommand implements Command {

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
  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "ResultOfMethodCallIgnored"})
  @Override
  public void execute(
      ChatMessageDTO chatMessageDTO,
      String command,
      String variable,
      String[][] parameter,
      ChatService chatService,
      MessageContext messageContext) {
    String returnString;

    GameManagement gameManagement = GameManagement.getInstance();

    Optional<Game> optionalGame = gameManagement.getGame(chatMessageDTO.getLobbyId());
    if (optionalGame.isEmpty()) {
      return;
    }
    GameDTO game = (GameDTO) optionalGame.get();

    Robot robot = game.getPlayer(chatMessageDTO.getUserDTO()).getRobot();
    Step step = new Step(game.getGameId());
    if (variable != null && !variable.equals("")) {
      try {
        int damage = Integer.parseInt(variable);

        for (int i = 0; i < damage; i++) {
          if (robot.damage()) {
            break;
          }
        }
        returnString = "Roboter hat " + variable + " schaden bekommen";
      } catch (NumberFormatException e) {
        returnString = "Ungültiger wert!";
      }
    } else {
      robot.getDamage();
      returnString = "Roboter hat " + variable + " schaden bekommen";
    }

    if (robot.isAlive()) {
      step.addAnimation(
          new SingleAnimation(
              AnimationType.LASER_DAMAGE,
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              robot.getDirection()));
    }
    chatService.sendToClient(messageContext, chatMessageDTO.getLobbyId(), returnString);
  }
}
