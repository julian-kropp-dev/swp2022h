package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.message.RobotInformationMessage;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;
import de.uol.swp.server.game.GameLogic;
import de.uol.swp.server.game.GameManagement;
import de.uol.swp.server.game.GameService;
import de.uol.swp.server.message.MoveRobotInternalMessage;
import de.uol.swp.server.message.StartRespawnInteractionInternalMessage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This Class is for the start gameLogic command /startLogic.
 *
 * <p>Is handle the reaction from the ChatService to start the logic
 */
@SuppressWarnings("UnstableApiUsage")
public class StartLogicCommand implements Command {

  private final EventBus eventBus;
  private GameDTO game;

  public StartLogicCommand(EventBus eventBus) {
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
    GameService gameService = GameService.getInstance();

    gameManagement
        .getGames()
        .forEach(
            (lobby, game2) -> {
              if (game2.getGameId().equals(chatMessageDTO.getLobbyId())) {
                this.game = (GameDTO) game2;
                GameLogic logic = new GameLogic(game, gameService, eventBus);
                try {
                  switch (variable) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                      int value = Integer.parseInt(variable);
                      value--;
                      logic.setup();
                      logic.factoryPhase(value);
                      eventBus.post(new MoveRobotInternalMessage(logic.getStep(), game.getLobby()));
                      break;
                    case "all":
                      logic.setup();
                      for (int i = 0; i < 5; i++) {
                        logic.factoryPhase(i);
                      }
                      eventBus.post(new MoveRobotInternalMessage(logic.getStep(), game.getLobby()));
                      break;
                    case "respawn":
                      logic.setup();
                      logic.setQueueForRespawn();
                      game.setInFirstRoundOfRespawnProcess(true);
                      game.setTimerStopped(true);
                      gameService.onStartRespawnInteractionInternalMessage(
                          new StartRespawnInteractionInternalMessage(game.getGameId()));
                      AtomicBoolean sendable = new AtomicBoolean(true);
                      logic
                          .getStep()
                          .getLatestRobotInformation()
                          .forEach(
                              (robots, robotInformation) -> {
                                if (robotInformation.getCurrentField() == null) {
                                  sendable.set(false);
                                }
                              });
                      if (sendable.get()) {
                        eventBus.post(
                            new MoveRobotInternalMessage(logic.getStep(), game.getLobby()));
                      }
                      break;
                    case "cp":
                      if (parameter.length >= 1 && !parameter[0][2].isEmpty()) {

                        Robot robot = game.getPlayer(chatMessageDTO.getUserDTO()).getRobot();

                        robot.setCurrentCheckpoint(Integer.parseInt(parameter[0][2]));
                        robot.setRespawn(
                            game.getFloorPlan()
                                .getCheckpoints()
                                .get(Integer.parseInt(parameter[0][2]) - 1));
                        eventBus.post(
                            new RobotInformationMessage(
                                robot.getType(), robot.getRobotInformation(), game.getGameId()));
                      }
                      break;
                    case "repair":
                      logic.repair(true);
                      break;
                    default:
                      chatService.sendToClient(
                          messageContext, chatMessageDTO.getLobbyId(), "Bitte was eingeben");
                      break;
                  }

                } catch (NumberFormatException e) {
                  chatService.sendToClient(
                      messageContext, chatMessageDTO.getLobbyId(), "Variable nicht Gültig!");
                }
              }
            });

    chatService.sendToClient(messageContext, chatMessageDTO.getLobbyId(), "Starte die Logik");
  }
}
